package com.example.likeyoutube

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.likeyoutube.databinding.ActivityMainBinding
import com.example.likeyoutube.fragment.home.HomeFragment
import com.example.likeyoutube.fragment.SignInFragment
import com.example.likeyoutube.internet.AuthenticationImplementer

class MainActivity : AppCompatActivity() {

    lateinit var activityMainBinding: ActivityMainBinding
    private val authenticationImplementer = AuthenticationImplementer.getInctance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        authenticationImplementer.initActivity(this)
        authenticationImplementer.restoreState()

        authenticationImplementer.mutable.observe(this) {
            Log.d("ttt", "it - $it")
            if (it != "{}") {
                selectFragment(HomeFragment())
            } else {
                selectFragment(SignInFragment())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.RC_SIGN_IN) {
            if (data != null) {
                authenticationImplementer.handleAuthorizationResponse(data)
            }
        }
    }

    fun selectFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(activityMainBinding.fragment.id, fragment)
        fragmentTransaction.commit()
    }

    companion object {
        val TAG = "ttt"
    }
}

