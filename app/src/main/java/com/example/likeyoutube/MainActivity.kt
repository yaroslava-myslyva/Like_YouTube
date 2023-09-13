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
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.likeyoutube.databinding.ActivityMainBinding
import com.example.likeyoutube.fragment.ExploreFragment
import com.example.likeyoutube.fragment.HomeFragment
import com.example.likeyoutube.fragment.LibraryFragment
import com.example.likeyoutube.fragment.SubscriptionsFragment
//import com.google.android.gms.auth.api.Auth
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInClient
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import com.google.android.gms.common.ConnectionResult
//import com.google.android.gms.common.Scopes
//import com.google.android.gms.common.api.ApiException
//import com.google.android.gms.common.api.GoogleApiClient
//import com.google.android.gms.common.api.Scope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.api.client.auth.oauth2.Credential
//import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
//import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
//import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.MemoryDataStoreFactory
import com.google.api.services.youtube.YouTubeScopes
//import com.google.auth.oauth2.JwtCredentials
//import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import net.openid.appauth.*
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.VersionedBrowserMatcher
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okio.IOException
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.io.InputStreamReader
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authenticationImplementer = AuthenticationImplementer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authenticationImplementer.setActivity(this)
        authenticationImplementer.initAuthServiceConfig()
        authenticationImplementer.initAuthService()
        authenticationImplementer.restoreState()
        authenticationImplementer.makeApiCall()
        binding.button.setOnClickListener {
            authenticationImplementer.attemptAuthorization()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == Constants.RC_SIGN_IN) {
            if (data != null) {
                authenticationImplementer.handleAuthorizationResponse(data)
            }
        }
    }
}

