package com.example.likeyoutube2.internet.authentication

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
import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.likeyoutube2.Constants
import com.example.likeyoutube2.MainActivity
import com.example.likeyoutube2.MainActivity.Companion.TAG
import com.example.likeyoutube2.internet.YouTubeApiClient
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

class AppAuthAuthenticationImplementer private constructor() : IAuthenticationImplementer {
    private var authState: AuthState = AuthState()
    private var mutable = MutableLiveData<String>("{}")
    private var jwt: JWT? = null
    private lateinit var authorizationService: AuthorizationService
    private lateinit var authServiceConfig: AuthorizationServiceConfiguration
    override lateinit var activity: MainActivity
    var audience :MutableList<String>?= mutableListOf<String>()

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: AppAuthAuthenticationImplementer? = null
        fun getInctance(): AppAuthAuthenticationImplementer {
            return instance ?: synchronized(this) {
                instance = AppAuthAuthenticationImplementer()
                return instance as AppAuthAuthenticationImplementer
            }
        }
    }

    override fun initActivity(act: MainActivity) {
        activity = act
        initAuthServiceConfig()
        initAuthService()
    }

    // загрузить состояние
    override fun restoreState() {
        val jsonString = activity.application.getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
        ).getString(Constants.AUTH_STATE, null)
        mutable.value = jsonString ?: "{}"
        Log.d("ttt", "jsonString = $jsonString")
        if (jsonString != null && !TextUtils.isEmpty(jsonString)) {
            try {
                authState = AuthState.jsonDeserialize(jsonString)
                if (!TextUtils.isEmpty(authState.idToken)) {
                    jwt = JWT(authState.idToken!!)
                }
            } catch (jsonException: JSONException) {
                Log.d(
                    "ttt",
                    "restoreState jsonException ${jsonException.message} ${jsonException.javaClass}"
                )
            }
        }
    }

    // сохранить состояние
    override fun persistState() {
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
    override fun attemptAuthorization() {
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
            Log.d(TAG, "attemptAuthorization: ${request.jsonSerializeString()}")
            startActivityForResult(activity, authIntent, Constants.RC_SIGN_IN, null)
        } catch (e: java.lang.Exception) {
            Log.d("ttt", "can't sign in ${e.javaClass} ${e.message}")
        }
    }

    override fun handleAuthorizationResponse(intent: Intent) {
        Log.d(TAG, "handleAuthorizationResponse: ")
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
                            audience = jwt!!.audience
                        val decodedJWT: DecodedJWT = com.auth0.jwt.JWT.decode(jwt.toString())
                        savingUserData(decodedJWT.claims)
                        Log.d(
                            "ttt", "jwt - ${decodedJWT.claims[Constants.DATA_EMAIL]}"
                        ) // в claims хранится мапа с данными пользователя. Достаём его емейл.
                    }
                }
                persistState()
            }
        }
    }

    private fun savingUserData(claims: Map<String, Claim>) {
        with(
            activity.application.getSharedPreferences(
                Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
            ).edit()
        ) {
            putString(Constants.DATA_PICTURE, claims[Constants.DATA_PICTURE]?.asString()).apply()
            putString(
                Constants.DATA_FIRST_NAME,
                claims[Constants.DATA_FIRST_NAME]?.asString()
            ).apply()
            putString(
                Constants.DATA_LAST_NAME,
                claims[Constants.DATA_LAST_NAME]?.asString()
            ).apply()
            putString(Constants.DATA_EMAIL, claims[Constants.DATA_EMAIL]?.asString()).apply()
        }

    }

    override fun getYouTubeApi(): YouTubeApiClient? {
        Log.d("ttt", "makeApiCall")
        var youTubeApiClient: YouTubeApiClient? = null
        if (authState.needsTokenRefresh) {
            authState.refreshToken
        }

        val email = activity.application.getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
        ).getString(Constants.DATA_EMAIL, "")
        Log.d(TAG, "getYouTubeApi: email = $email")
        try {

            val credential: GoogleAccountCredential =
                GoogleAccountCredential //.usingAudience(activity, audience?.get(0) ?: "")
                    .usingOAuth2(
                    activity,
                    Collections.singleton(YouTubeScopes.YOUTUBE)
                )
                    .setSelectedAccountName(email)
            Log.d("ttt", "credential - ${credential.token}")
            youTubeApiClient = YouTubeApiClient(credential, activity)
            Log.d("ttt", "list - ${youTubeApiClient.getAllPlaylists()}")

        } catch (e: Exception) {
            Log.d(
                "ttt", "can't call = ${e.message} ${e.javaClass} "
            )
            e.stackTrace.forEach { Log.d(TAG, "getYouTubeApi: $it") }
            authState = AuthState()
            MainScope(). launch(Dispatchers.Main) { persistState() }
        }

        return youTubeApiClient
    }

    override fun signOut() {
        Log.d(TAG, "signOutWithoutRedirect: ")
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