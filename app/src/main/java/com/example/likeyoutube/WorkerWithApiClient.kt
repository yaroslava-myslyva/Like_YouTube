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
            val firstPlaylist = list?.get(1)
            val myPlaylistsTitlesAndIDsString = getStringFromShared(Constants.DATA_PLAYLISTS_TITLES_AND_IDS)
            myPlaylistsTitlesAndIDs = myPlaylistsTitlesAndIDsString?.let { deserializeStringToMap(it) }
            val mustBeId = myPlaylistsTitlesAndIDs?.get("1")
            Log.d(TAG, "logListSongsTitlesOfOnePlaylist: ${mustBeId == firstPlaylist?.id} $mustBeId ${firstPlaylist?.id} ${firstPlaylist?.snippet?.title}")

            val listOfSongs =
                firstPlaylist?.id?.let { youTubeApiClient?.getSongsTitlesFromPlaylist(it) }
            Log.d(TAG, "list $listOfSongs")
            launch(Dispatchers.Main) {
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
                launch(Dispatchers.Main) {
                    putStringToShared(playlistTitle, listSongsIDsToString)
                }
            }

            launch(Dispatchers.Main) {
                val myPlaylistsTitlesToString = myPlaylistsTitles?.let { serializeListToString(it) }
                val myPlaylistsTitlesAndIDsToString = myPlaylistsTitlesAndIDs?.let {
                    serializeMapToString(it)
                }

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
        MainScope().launch(Dispatchers.IO) {
            isYouTubeApiClientNull()
            myPlaylistsTitles?.forEach { title ->
                val playlistId = myPlaylistsTitlesAndIDs?.get(title)
                Log.d(TAG, "restoreMyPlaylists: playlistId - $playlistId")
                val songsIDsListMustBeString = getStringFromShared(title)
                val songsIDsListMustBe =
                    songsIDsListMustBeString?.let { deserializeStringToList(it) }

                val songsIDsListFromAPI = playlistId?.let {
                    youTubeApiClient?.getSongsIDFromPlaylist(it)
                }
                songsIDsListMustBe?.forEach { oneSongID ->
                    songsIDsListFromAPI?.let {
                        if (!it.contains(oneSongID)) {
                            youTubeApiClient?.addVideoToPlaylist(playlistId, oneSongID)
                            val songTitle = youTubeApiClient?.getVideoTitleById(oneSongID)
                            Log.d(TAG, "restoreMyPlaylists: немає $songTitle, додаю")
                        }
                    }
                }
            }
        }
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