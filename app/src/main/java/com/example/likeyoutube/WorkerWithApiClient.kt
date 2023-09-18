package com.example.likeyoutube

import android.annotation.SuppressLint
import android.util.Log
import com.example.likeyoutube.MainActivity.Companion.TAG
import com.example.likeyoutube.internet.AuthenticationImplementer
import com.example.likeyoutube.internet.YouTubeApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class WorkerWithApiClient {
    private val authenticationImplementer = AuthenticationImplementer.getInctance()
    private var youTubeApiClient: YouTubeApiClient? = null

    private fun isYouTubeApiClientNull() {
        if (youTubeApiClient == null) {
            youTubeApiClient = authenticationImplementer.getYouTubeApi()
        }
    }

    fun logAllPlaylistsTitles() {
        MainScope().launch(Dispatchers.IO) {
            isYouTubeApiClientNull()
            val list = youTubeApiClient?.getAllPlaylists()
            Log.d(TAG, "list ${list}")
            launch(Dispatchers.Main){
                Log.d(TAG, "number ${list?.size}")
                list?.forEach { Log.d("ttt", "title - ${it.snippet.title}") }
            }
        }
    }

    fun logMapSongsOfPlaylist(){
        MainScope().launch(Dispatchers.IO) {
            isYouTubeApiClientNull()
            val list = youTubeApiClient?.getAllPlaylists()
            val firstPlaylist = list?.first()

            val listOfSongs = firstPlaylist?.id?.let { youTubeApiClient?.getSongsFromPlaylist(it) }
            Log.d(TAG, "list $listOfSongs")
            launch(Dispatchers.Main){
                Log.d(TAG, "number ${listOfSongs?.size}")
                listOfSongs?.forEach { Log.d("ttt", "- $it") }
            }
        }
    }




}