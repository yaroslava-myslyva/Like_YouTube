package com.example.likeyoutube.internet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.MutableLiveData
import com.auth0.android.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.likeyoutube.Constants
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.youtube.YouTubeScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import net.openid.appauth.*
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.VersionedBrowserMatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

class AuthenticationImplementer {
    private var authState: AuthState = AuthState()
    var mutable = MutableLiveData<String>("{}")
    private var jwt: JWT? = null
    private lateinit var authorizationService: AuthorizationService
    private lateinit var authServiceConfig: AuthorizationServiceConfiguration
    private lateinit var activity: Activity

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: AuthenticationImplementer? = null
        fun getInctance(): AuthenticationImplementer {
            return instance ?: synchronized(this) {
                instance = AuthenticationImplementer()
                return instance as AuthenticationImplementer
            }
        }
    }

    fun initActivity(act: Activity) {
        activity = act
        initAuthServiceConfig()
        initAuthService()
    }

    // загрузить состояние
    fun restoreState(): Boolean {
        val jsonString = activity.application.getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
        ).getString(Constants.AUTH_STATE, null)
        mutable.value = jsonString ?: "{}"
        Log.d("ttt", "jsonString = $jsonString")
        return if (jsonString != null && !TextUtils.isEmpty(jsonString)) {
            try {
                authState = AuthState.jsonDeserialize(jsonString)
                if (!TextUtils.isEmpty(authState.idToken)) {
                    jwt = JWT(authState.idToken!!)
                    true
                } else false
            } catch (jsonException: JSONException) {
                false
            }
        } else {
            false
        }
    }

    // сохранить состояние
    fun persistState() {
        activity.application.getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
        ).edit().putString(Constants.AUTH_STATE, authState.jsonSerializeString()).apply()
        mutable.value = authState.jsonSerializeString()
        Log.d("ttt", "persistState ${authState.jsonSerializeString()}")
    }

    private fun initAuthServiceConfig() {
        authServiceConfig = AuthorizationServiceConfiguration(
            Uri.parse(Constants.URL_AUTHORIZATION),
            Uri.parse(Constants.URL_TOKEN_EXCHANGE),
            null,
            Uri.parse(Constants.URL_LOGOUT)
        )
    }

    //Говорим, что хотим авторизоваться через браузер.
    private fun initAuthService() {
        val appAuthConfiguration = AppAuthConfiguration.Builder().setBrowserMatcher(
            BrowserAllowList(
                VersionedBrowserMatcher.CHROME_CUSTOM_TAB,
                VersionedBrowserMatcher.SAMSUNG_CUSTOM_TAB
            )
        ).build()

        authorizationService = AuthorizationService(
            activity.application, appAuthConfiguration
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
        ).setCodeVerifier(
            codeVerifier, codeChallenge, Constants.CODE_VERIFIER_CHALLENGE_METHOD
        )

        builder.setScopes(
            Constants.SCOPE_PROFILE,
            Constants.SCOPE_EMAIL,
            Constants.SCOPE_OPENID,
            Constants.SCOPE_YOUTUBE
        )
        try {
            val request = builder.build()
            val authIntent = authorizationService.getAuthorizationRequestIntent(request)
            startActivityForResult(activity, authIntent, Constants.RC_SIGN_IN, null)
        } catch (e: java.lang.Exception) {
            Log.d("ttt", "can't sign in ${e.javaClass} ${e.message}")
        }
    }


    fun handleAuthorizationResponse(intent: Intent) {
        val authorizationResponse: AuthorizationResponse? = AuthorizationResponse.fromIntent(intent)
        val error = AuthorizationException.fromIntent(intent)

        authState = AuthState(authorizationResponse, error)
        val tokenExchangeRequest =
            authorizationResponse?.createTokenExchangeRequest(authorizationResponse.additionalParameters)
        if (tokenExchangeRequest != null) {
            authorizationService.performTokenRequest(tokenExchangeRequest) { response, exception ->
                if (exception != null) {
                    authState = AuthState()
                    Log.d("ttt", "exception - ${exception.message}  ${exception.javaClass}")
                } else {
                    if (response != null) {
                        authState.update(response, exception)
                        jwt = JWT(response.idToken!!)
                        val j: DecodedJWT = com.auth0.jwt.JWT.decode(jwt.toString())
                        Log.d(
                            "ttt", "jwt - ${j.claims[Constants.DATA_EMAIL]}"
                        ) // в claims хранится мапа с данными пользователя. Достаём его емейл.
                        Log.d(
                            "ttt", "scope - ${authState.scope}"
                        )
                    }
                }
                persistState()
            }
        }
    }

    fun makeApiCall() {
        Log.d("ttt", "makeApiCall")
        authState.performActionWithFreshTokens(
            authorizationService,
            object : AuthState.AuthStateAction {
                override fun execute(
                    accessToken: String?, idToken: String?, ex: AuthorizationException?
                ) {
                    Log.d("ttt", "accessToken - $accessToken")

                    MainScope().launch(Dispatchers.IO) {
                        try {
                            val credential: GoogleAccountCredential =
                                GoogleAccountCredential.usingOAuth2(
                                        activity,
                                        Collections.singleton(YouTubeScopes.YOUTUBE)
                                    ).setSelectedAccountName("yaroslava.met@gmail.com")
                            Log.d("ttt", "credential - ${credential.token}")
                            val youTubeApiClient = YouTubeApiClient(credential, activity)
                            val list = youTubeApiClient.getPlaylists()
                            Log.d("ttt", "list - $list")
                        } catch (e: Exception) {
                            Log.d(
                                "ttt", "can't call = ${e.message} ${e.javaClass} "
                            )
                        }
                    }
                }
            })
    }

    fun signOutWithoutRedirect() {
        val client = OkHttpClient()
        val request = Request.Builder().url(Constants.URL_LOGOUT + authState.accessToken).build()
        MainScope().launch(Dispatchers.IO) {
            try {
                client.newCall(request).execute()
                authState = AuthState()
                launch(Dispatchers.Main) { persistState() }

            } catch (e: IOException) {
                Log.d("ttt", "can't logout ${e.message} ${e.javaClass}")
            }
        }
    }
}