package com.example.likeyoutube2.fragments

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder

class VideoPlaybackService : Service() {
    val mediaPlayer: MediaPlayer = MediaPlayer()
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): VideoPlaybackService = this@VideoPlaybackService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    fun playVideo(videoUri: Uri) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(applicationContext, videoUri)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pauseVideo() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    fun resumeVideo() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    fun stopVideo() {
        mediaPlayer.stop()
        mediaPlayer.reset()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
