package com.example.likeyoutube

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.likeyoutube.databinding.ActivityMainBinding
import com.example.likeyoutube.databinding.YoutubeActivityBinding
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView


class YouTubeActivity : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener {
    private lateinit var youtubeActivityBinding: YoutubeActivityBinding
    private var playerview: YouTubePlayerView? = null
    private var youtubeplayer: YouTubePlayer? = null
    private var editVideoID: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        youtubeActivityBinding = YoutubeActivityBinding.inflate(layoutInflater)
        setContentView(youtubeActivityBinding.root)

        playerview = youtubeActivityBinding.videoPlayer

        playerview!!.initialize("AIzaSyD6jtc3D7C2joMjRNZIzROehQhHCsNXAcE", this)
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.activity_youtube, menu)
//        return true
//    }

    override fun onInitializationFailure(
        arg0: YouTubePlayer.Provider,
        arg1: YouTubeInitializationResult
    ) {
        Toast.makeText(this, "Ошибка при инициализации", Toast.LENGTH_LONG)
            .show()
    }

    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider,
        player: YouTubePlayer, wasRestored: Boolean
    ) {
        youtubeplayer = player
        if (!wasRestored) {
            Toast.makeText(
                this, "Инициализация прошла успешно",
                Toast.LENGTH_LONG
            ).show()
           // player.cueVideo("vL8sp4VAOnU")
        }
    }

//    protected val youTubePlayerProvider: YouTubePlayer.Provider
//        protected get() = findViewById<View>(R.id.youtubeplayer) as YouTubePlayerView
//
//    fun onClick(v: View?) {
//        // youtubeplayer.cueVideo(editVideoID.getText().toString());
//        youtubeplayer!!.cueVideo(editVideoID!!.text.toString())
//    }
}