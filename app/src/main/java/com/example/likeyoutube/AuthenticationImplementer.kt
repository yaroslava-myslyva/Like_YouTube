package com.example.likeyoutube

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import com.auth0.android.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.youtube.YouTubeScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import net.openid.appauth.*
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.VersionedBrowserMatcher
import org.json.JSONException
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

//надо бы сделать, чтобы у него был только один инстанс
class AuthenticationImplementer {
    private var authState: AuthState = AuthState()
    private var jwt: JWT? = null
    private lateinit var authorizationService: AuthorizationService
    private lateinit var authServiceConfig: AuthorizationServiceConfiguration
    private lateinit var activity: Activity

    fun setActivity(act: Activity) {
        activity = act
    }

    // загрузить состояние
    fun restoreState() {
        val jsonString = activity.application
            .getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(Constants.AUTH_STATE, null)

        if (jsonString != null && !TextUtils.isEmpty(jsonString)) {
            try {
                authState = AuthState.jsonDeserialize(jsonString)

                if (!TextUtils.isEmpty(authState.idToken)) {
                    jwt = JWT(authState.idToken!!)
                }

            } catch (jsonException: JSONException) {
            }
        }
    }

    // сохранить состояние
    fun persistState() {
        activity.application.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(Constants.AUTH_STATE, authState.jsonSerializeString())
            .apply()
    }

    fun initAuthServiceConfig() {
        authServiceConfig = AuthorizationServiceConfiguration(
            Uri.parse(Constants.URL_AUTHORIZATION),
            Uri.parse(Constants.URL_TOKEN_EXCHANGE),
            null,
            Uri.parse(Constants.URL_LOGOUT)
        )
    }

    //Говорим, что хотим авторизоваться через браузер.
    fun initAuthService() {
        val appAuthConfiguration = AppAuthConfiguration.Builder()
            .setBrowserMatcher(
                BrowserAllowList(
                    VersionedBrowserMatcher.CHROME_CUSTOM_TAB,
                    VersionedBrowserMatcher.SAMSUNG_CUSTOM_TAB
                )
            ).build()

        authorizationService = AuthorizationService(
            activity.application,
            appAuthConfiguration
        )
    }

    //попытка авторизации
    fun attemptAuthorization() {
        val secureRandom = SecureRandom()
        val bytes = ByteArray(64)
        secureRandom.nextBytes(bytes)

        val encoding = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        val codeVerifier = Base64.encodeToString(bytes, encoding)

        val digest = MessageDigest.getInstance(Constants.MESSAGE_DIGEST_ALGORITHM)
        val hash = digest.digest(codeVerifier.toByteArray())
        val codeChallenge = Base64.encodeToString(hash, encoding)
        val builder = AuthorizationRequest.Builder(
            authServiceConfig,
            Constants.CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(Constants.URL_AUTH_REDIRECT)
        )
            .setCodeVerifier(
                codeVerifier,
                codeChallenge,
                Constants.CODE_VERIFIER_CHALLENGE_METHOD
            )

        builder.setScopes(
            Constants.SCOPE_PROFILE,
            Constants.SCOPE_EMAIL,
            Constants.SCOPE_OPENID,
            Constants.SCOPE_YOUTUBE
        )

        val request = builder.build()

        val authIntent = authorizationService.getAuthorizationRequestIntent(request)
        startActivityForResult(activity, authIntent, Constants.RC_SIGN_IN, null)
    }


    fun handleAuthorizationResponse(intent: Intent) {
        val authorizationResponse: AuthorizationResponse? = AuthorizationResponse.fromIntent(intent)
        val error = AuthorizationException.fromIntent(intent)

        authState = AuthState(authorizationResponse, error)
        val tokenExchangeRequest = authorizationResponse?.createTokenExchangeRequest()
        if (tokenExchangeRequest != null) {
            authorizationService.performTokenRequest(tokenExchangeRequest) { response, exception ->
                if (exception != null) {
                    authState = AuthState()

                } else {
                    if (response != null) {
                        authState.update(response, exception)
                        jwt = JWT(response.idToken!!)
                        val j: DecodedJWT = com.auth0.jwt.JWT.decode(jwt.toString())
                        Log.d(
                            "ttt",
                            "jwt - ${j.claims[Constants.DATA_EMAIL]}"
                        ) // в claims хранится мапа с данными пользователя. Достаём его емейл.
                    }
                }
                persistState()
                makeApiCall()
            }
        }
    }

    fun makeApiCall() {
        Log.d("ttt", "makeApiCall")
        authState.performActionWithFreshTokens(
            authorizationService,
            object : AuthState.AuthStateAction {
                override fun execute(
                    accessToken: String?,
                    idToken: String?,
                    ex: AuthorizationException?
                ) {
                    val credential: GoogleAccountCredential = GoogleAccountCredential
                        .usingOAuth2(
                            activity,
                            Collections.singleton(YouTubeScopes.YOUTUBE)
                        )
                        .setSelectedAccountName("yaroslava.met@gmail.com") //тут надо бы не вручную писать, сам понимаешь

                    MainScope().launch(Dispatchers.IO) {
                        try {
                            val youTubeApiClient =
                                YouTubeApiClient(credential, activity)
                            val list = youTubeApiClient.getPlaylists()
                            Log.d("ttt", "list - $list")
                        } catch (e: Exception) {
                            Log.i("ttt", "ex = ${e.message}")
                        }
                    }
                }
            }
        )
    }
}