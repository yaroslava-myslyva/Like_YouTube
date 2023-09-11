package com.example.likeyoutube

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64.*
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PackageManagerCompat.LOG_TAG
import androidx.fragment.app.Fragment
import com.auth0.android.jwt.JWT
import com.example.likeyoutube.databinding.ActivityMainBinding
import com.example.likeyoutube.fragment.ExploreFragment
import com.example.likeyoutube.fragment.HomeFragment
import com.example.likeyoutube.fragment.LibraryFragment
import com.example.likeyoutube.fragment.SubscriptionsFragment
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.MemoryDataStoreFactory
import com.google.api.services.youtube.YouTubeScopes
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import net.openid.appauth.*
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.VersionedBrowserMatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.io.InputStreamReader
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var authState: AuthState = AuthState()
    private var jwt: JWT? = null
    private lateinit var authorizationService: AuthorizationService
    lateinit var authServiceConfig: AuthorizationServiceConfiguration
    private val RC_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAuthServiceConfig()
        initAuthService()
        binding.button.setOnClickListener {
            attemptAuthorization()

        }


    }

    // загрузить состояние
    fun restoreState() {
        val jsonString = application
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
        application.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(Constants.AUTH_STATE, authState.jsonSerializeString())
            .commit()
    }

    private fun initAuthServiceConfig() {
        authServiceConfig = AuthorizationServiceConfiguration(
            Uri.parse(Constants.URL_AUTHORIZATION),
            Uri.parse(Constants.URL_TOKEN_EXCHANGE),
            null,
            Uri.parse(Constants.URL_LOGOUT)
        )
    }

    private fun initAuthService() {
        val appAuthConfiguration = AppAuthConfiguration.Builder()
            .setBrowserMatcher(
                BrowserAllowList(
                    VersionedBrowserMatcher.CHROME_CUSTOM_TAB,
                    VersionedBrowserMatcher.SAMSUNG_CUSTOM_TAB
                )
            ).build()

        authorizationService = AuthorizationService(
            getApplication(),
            appAuthConfiguration
        )
    }

    fun attemptAuthorization() {
        val secureRandom = SecureRandom()
        val bytes = ByteArray(64)
        secureRandom.nextBytes(bytes)

        val encoding = URL_SAFE or NO_PADDING or NO_WRAP
        val codeVerifier = encodeToString(bytes, encoding)

        val digest = MessageDigest.getInstance(Constants.MESSAGE_DIGEST_ALGORITHM)
        val hash = digest.digest(codeVerifier.toByteArray())
        val codeChallenge = encodeToString(hash, encoding)
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
            //  Constants.SCOPE_DRIVE,
            Constants.SCOPE_YOUTUBE
        )

        val request = builder.build()

        val authIntent = authorizationService.getAuthorizationRequestIntent(request)
        startActivityForResult(authIntent, RC_SIGN_IN)
        //launchForResult(authIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == RC_SIGN_IN) {
            if (data != null) {
                handleAuthorizationResponse(data)
            }
        }
    }

    private fun launchForResult(intent: Intent) {

        val authorizationLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()

        ) { result ->
            Log.d("ttt", "resultCode = ${result.resultCode}")
            run {


            }
        }
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

                    }
                }
                persistState()
                makeApiCall()
            }
        }
    }

    fun makeApiCall() {
        Log.d("ttt", "makeApiCall")
        authState.performActionWithFreshTokens(authorizationService,
            object : AuthState.AuthStateAction {
                override fun execute(
                    accessToken: String?,
                    idToken: String?,
                    ex: AuthorizationException?
                ) {
                    Log.d("ttt", "access - $accessToken")
                    val credential: GoogleAccountCredential = GoogleAccountCredential
                        .usingOAuth2(
                            this@MainActivity,
                            Collections.singleton(YouTubeScopes.YOUTUBE)
                        )
                        .setSelectedAccountName("yaroslava.met@gmail.com")
                    GlobalScope.launch {
                        MainScope().launch(Dispatchers.IO) {
                            val youTubeApiClient = YouTubeApiClient(credential, this@MainActivity)
                            val list = youTubeApiClient.getPlaylists()
                            Log.d("ttt", "list - $list")
                        }

//                        async(Dispatchers.IO) {
//                            val client = OkHttpClient()
//                            val request = Request.Builder()
//                                .url(Constants.URL_API_CALL)
//                                .addHeader("Authorization", "Bearer " + authState.accessToken)
//                                .build()
//
//                            try {
//                                val response = client.newCall(request).execute()
//                                val jsonBody = response.body?.string() ?: ""
//                                Log.i("ttt", JSONObject(jsonBody).toString())
//                            } catch (e: Exception) { }
//                        }
                    }
                }
            }
        )
    }

}

