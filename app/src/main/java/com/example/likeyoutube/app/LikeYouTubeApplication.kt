package com.example.likeyoutube.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class LikeYouTubeApplication : Application() {
    companion object {
        val applicationScope = CoroutineScope(SupervisorJob())
    }
}