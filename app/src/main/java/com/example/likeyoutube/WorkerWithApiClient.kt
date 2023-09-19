package com.example.likeyoutube

import android.content.Context
import android.util.Log
import com.example.likeyoutube.MainActivity.Companion.TAG
import com.example.likeyoutube.internet.AuthenticationImplementer
import com.example.likeyoutube.internet.YouTubeApiClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class WorkerWithApiClient {
    private val authenticationImplementer = AuthenticationImplementer.getInctance()
    private var youTubeApiClient: YouTubeApiClient? = null
    private var myPlaylistsTitles: MutableList<String>? = mutableListOf()
    private var myPlaylistsTitlesAndIDs: MutableMap<String, String>? = mutableMapOf()

    private fun isYouTubeApiClientNull() {
        if (youTubeApiClient == null) {
            youTubeApiClient = authenticationImplementer.getYouTubeApi()
        }
    }

    fun logAllPlaylistsTitles() {
        MainScope().launch(Dispatchers.IO) {
            isYouTubeApiClientNull()
            val list = youTubeApiClient?.getAllPlaylists()
            Log.d(TAG, "list $list")
            launch(Dispatchers.Main) {
                Log.d(TAG, "number ${list?.size}")
                list?.forEach { Log.d("ttt", "title - ${it.snippet.title}") }
            }
        }
    }

    fun logListSongsTitlesOfOnePlaylist() {
        MainScope().launch(Dispatchers.IO) {
            isYouTubeApiClientNull()
            val list = youTubeApiClient?.getAllPlaylists()
            val firstPlaylist = list?.first()

            val listOfSongs =
                firstPlaylist?.id?.let { youTubeApiClient?.getSongsTitlesFromPlaylist(it) }
            Log.d(TAG, "list $listOfSongs")
            launch(Dispatchers.Main) {
                Log.d(TAG, "number ${listOfSongs?.size}")
                listOfSongs?.forEach { Log.d("ttt", "- $it") }
            }
        }
    }

    fun saveMyPlaylists() {
        MainScope().launch(Dispatchers.IO) {
            isYouTubeApiClientNull()
            val list = youTubeApiClient?.getAllPlaylists()
            list?.forEach { playlist ->
                val playlistId = playlist.id
                val playlistTitle = playlist.snippet.title

                Log.d(TAG, "saveMyPlaylists: playlistId playlistTitle $playlistId $playlistTitle")

                myPlaylistsTitles?.add(playlistTitle)
                myPlaylistsTitlesAndIDs?.put(playlistTitle, playlistId)

                val listSongsID = youTubeApiClient?.getSongsIDFromPlaylist(playlistId)
                val listSongsIDsToString = listSongsID?.let { serializeListToString(it) }

                Log.d(TAG, "saveMyPlaylists: listSongsIDsToString $listSongsIDsToString")

                launch(Dispatchers.Main) {
                    putStringToShared(playlistTitle, listSongsIDsToString)
                }
            }

            launch(Dispatchers.Main) {
                val myPlaylistsTitlesToString = myPlaylistsTitles?.let { serializeListToString(it) }
                val myPlaylistsTitlesAndIDsToString = myPlaylistsTitlesAndIDs?.let {
                    serializeMapToString(it)
                }

                Log.d(TAG, "saveMyPlaylists: myPlaylistsTitlesToString $myPlaylistsTitlesToString")
                Log.d(
                    TAG,
                    "saveMyPlaylists: myPlaylistsTitlesAndIDsToString $myPlaylistsTitlesAndIDsToString"
                )

                putStringToShared(Constants.DATA_PLAYLISTS_TITLES, myPlaylistsTitlesToString)
                putStringToShared(
                    Constants.DATA_PLAYLISTS_TITLES_AND_IDS,
                    myPlaylistsTitlesAndIDsToString
                )
            }
        }
    }

    fun restoreMyPlaylists() {
        val myPlaylistsTitlesString = getStringFromShared(Constants.DATA_PLAYLISTS_TITLES)
        myPlaylistsTitles = myPlaylistsTitlesString?.let {
            deserializeStringToList(it)
        }
        val myPlaylistsTitlesAndIDsString =
            getStringFromShared(Constants.DATA_PLAYLISTS_TITLES_AND_IDS)
        myPlaylistsTitlesAndIDs = myPlaylistsTitlesAndIDsString?.let {
            deserializeStringToMap(it)
        }
        Log.d(TAG, "restoreMyPlaylists: $myPlaylistsTitlesString")
        Log.d(TAG, "restoreMyPlaylists: $myPlaylistsTitlesAndIDs")

    }

    private fun putStringToShared(name: String, string: String?) {
        authenticationImplementer.activity.application.getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
        ).edit().putString(name, string).apply()
    }
    private fun getStringFromShared(name: String): String? {
        return authenticationImplementer.activity.application.getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
        ).getString(name, null)
    }

    private fun serializeListToString(list: MutableList<String>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    private fun deserializeStringToList(jsonString: String): MutableList<String> {
        val gson = Gson()
        val listType = object : TypeToken<MutableList<String>>() {}.type
        return gson.fromJson(jsonString, listType)
    }

    private fun serializeMapToString(map: MutableMap<String, String>): String {
        val gson = Gson()
        val mapType = object : TypeToken<MutableMap<String, String>>() {}.type
        return gson.toJson(map, mapType)
    }

    private fun deserializeStringToMap(jsonString: String): MutableMap<String, String> {
        val gson = Gson()
        val mapType = object : TypeToken<MutableMap<String, String>>() {}.type
        return gson.fromJson(jsonString, mapType)
    }


}