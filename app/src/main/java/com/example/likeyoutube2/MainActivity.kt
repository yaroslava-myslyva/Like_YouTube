package com.example.likeyoutube2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.likeyoutube2.databinding.ActivityMainBinding
import com.example.likeyoutube2.internet.authentication.GoogleSignInAuthenticationImplementer
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import java.net.CookieHandler
import java.net.CookieManager

class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    lateinit var activityMainBinding: ActivityMainBinding
    private val authenticationImplementer = GoogleSignInAuthenticationImplementer.getInctance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cookieManager = CookieManager()
        CookieHandler.setDefault(cookieManager)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        authenticationImplementer.initActivity(this)
        authenticationImplementer.restoreState()

//        authenticationImplementer.mutable.observe(this) {
//            Log.d("ttt", "it - $it")
//            if (it != "{}") {
//                selectFragment(HomeFragment())
//            } else {
//                selectFragment(SignInFragment())
//            }
//        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: $requestCode resultCode $resultCode")
        if (requestCode == Constants.RC_SIGN_IN) {
            if (data != null ) {
                authenticationImplementer.handleAuthorizationResponse(data)
            }
        }
    }

    fun selectFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(activityMainBinding.fragment.id, fragment)
        fragmentTransaction.commit()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }


    companion object {
        val TAG = "ttt"
    }
}

