package com.example.likeyoutube2.internet.authentication

import android.content.Intent
import com.example.likeyoutube2.MainActivity
import com.example.likeyoutube2.internet.YouTubeApiClient

interface IAuthenticationImplementer {
    val activity :MainActivity
    fun init(act: MainActivity)
    fun restoreState()
    fun persistState()
    fun attemptAuthorization()
    fun handleAuthorizationResponse(intent: Intent)
    fun getYouTubeApi(): YouTubeApiClient?
    fun signOut()
}