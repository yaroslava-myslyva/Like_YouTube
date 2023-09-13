package com.example.likeyoutube

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.likeyoutube.databinding.ActivityMainBinding
import com.example.likeyoutube.fragment.HomeFragment
import com.example.likeyoutube.fragment.SignInFragment
import com.example.likeyoutube.internet.AuthenticationImplementer

//import com.google.android.gms.auth.api.Auth
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInClient
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import com.google.android.gms.common.ConnectionResult
//import com.google.android.gms.common.Scopes
//import com.google.android.gms.common.api.ApiException
//import com.google.android.gms.common.api.GoogleApiClient
//import com.google.android.gms.common.api.Scope
//import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
//import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
//import com.google.api.client.json.gson.GsonFactory
//import com.google.auth.oauth2.JwtCredentials
//import com.google.firebase.auth.FirebaseAuth
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okio.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authenticationImplementer = AuthenticationImplementer.getInctance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authenticationImplementer.initAI(this)

        if(authenticationImplementer.restoreState()){
            selectedFragment(HomeFragment())
        } else{
            selectedFragment(SignInFragment())
        }

//        authenticationImplementer.makeApiCall()


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == Constants.RC_SIGN_IN) {
            if (data != null) {
                authenticationImplementer.handleAuthorizationResponse(data)
            }
        }
    }

    private fun selectedFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragment.id, fragment)
        fragmentTransaction.commit()
    }
}

