package com.example.likeyoutube2.internet.authentication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.likeyoutube2.Constants
import com.example.likeyoutube2.Constants.Companion.RC_SIGN_IN
import com.example.likeyoutube2.MainActivity
import com.example.likeyoutube2.MainActivity.Companion.TAG
import com.example.likeyoutube2.fragments.SignInFragment
import com.example.likeyoutube2.fragments.home.HomeFragment
import com.example.likeyoutube2.internet.YouTubeApiClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.youtube.YouTubeScopes
import java.util.*

class GoogleSignInAuthenticationImplementer() : IAuthenticationImplementer {
    override lateinit var activity: MainActivity
    private lateinit var account: GoogleSignInAccount
    private val gsc = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(YouTubeScopes.YOUTUBE))
        .build()
    private var client: GoogleApiClient? = null
    private var youTubeApiClient: YouTubeApiClient? = null


    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: GoogleSignInAuthenticationImplementer? = null
        fun getInctance(): GoogleSignInAuthenticationImplementer {
            return instance ?: synchronized(this) {
                instance = GoogleSignInAuthenticationImplementer()
                return instance as GoogleSignInAuthenticationImplementer
            }
        }
    }

    override fun initActivity(act: MainActivity) {
        activity = act
    }

    override fun restoreState() {
        Log.d(TAG, "restoreState: ")
        val googleSignInClient = GoogleSignIn.getClient(activity, gsc)
        client = googleSignInClient.asGoogleApiClient()
        val signInTask = googleSignInClient.silentSignIn()
        signInTask.addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
                account = task.result
                savingUserData(account)
                activity.selectFragment(HomeFragment())
            } else {
                activity.selectFragment(SignInFragment())
            }
        }
    }

    override fun persistState() {
        Log.d(TAG, "persistState: ")
    }

    override fun attemptAuthorization() {
        val googleSignInClient = GoogleSignIn.getClient(activity, gsc)
        val intent = googleSignInClient.signInIntent
        startActivityForResult(activity, intent, RC_SIGN_IN, null)
    }

    override fun handleAuthorizationResponse(intent: Intent) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        try {
            account = task.getResult(ApiException::class.java)
            savingUserData(account)
            activity.selectFragment(HomeFragment())
        } catch (e: Exception) {
            Log.d("ttt", "exception - ${e.message}, ${e.stackTrace} ${e.javaClass}")
        }
        persistState()
    }

    override fun getYouTubeApi(): YouTubeApiClient? {
        Log.d(TAG, "OAuth getYouTubeApi: ")
        if (youTubeApiClient == null) {
            try {
                val credential: GoogleAccountCredential = GoogleAccountCredential
                    .usingOAuth2(activity, Collections.singleton(YouTubeScopes.YOUTUBE))
                    .setSelectedAccountName(account.email)
                youTubeApiClient = YouTubeApiClient(credential, activity)
            } catch (e: Exception) {
                Log.d("ttt", "exception - ${e.message}, ${e.stackTrace} ${e.javaClass}")
                signOut()
            }
        }
        Log.d(TAG, "getYouTubeApi: return $youTubeApiClient")
        return youTubeApiClient
    }

    override fun signOut() {
        Log.d(TAG, "signOut: ")
        val client = GoogleSignIn.getClient(activity, gsc)
        client.signOut()
        activity.selectFragment(SignInFragment())
    }

    private fun savingUserData(account: GoogleSignInAccount) {
        Log.d(TAG, "savingUserData: ")
        with(
            activity.application.getSharedPreferences(
                Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
            ).edit()
        ) {
            putString(Constants.DATA_PICTURE, account.photoUrl.toString()).apply()
            putString(
                Constants.DATA_FIRST_NAME, account.givenName
            ).apply()
            putString(
                Constants.DATA_LAST_NAME,
                account.familyName
            ).apply()
            putString(Constants.DATA_EMAIL, account.email).apply()
        }
    }
}
