package com.example.likeyoutube2.internet

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import com.example.likeyoutube2.Constants
import com.example.likeyoutube2.MainActivity.Companion.TAG
import com.example.likeyoutube2.fragments.big_playlist.VideoInfo
import com.example.likeyoutube2.internet.authentication.GoogleSignInAuthenticationImplementer
import com.google.api.services.youtube.model.Playlist
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class WorkerWithApiClient {
    private val authenticationImplementer = GoogleSignInAuthenticationImplementer.getInctance()
    private var youTubeApiClient: YouTubeApiClient? = null
    private var myPlaylistsTitles: MutableList<String>? = mutableListOf()
    private var myPlaylistsTitlesAndIDs: MutableMap<String, String>? = mutableMapOf()

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: WorkerWithApiClient? = null
        fun getInctance(): WorkerWithApiClient {
            return instance ?: synchronized(this) {
                instance = WorkerWithApiClient()
                return instance as WorkerWithApiClient
            }
        }
    }

    private fun isYouTubeApiClientNull() {
synchronized(this) {
    if (youTubeApiClient == null) {
        Log.d(TAG, "isYouTubeApiClientNull: true")
        youTubeApiClient = authenticationImplementer.getYouTubeApi()
        Log.d(TAG, "isYouTubeApiClientNull: youTubeApiClient = $youTubeApiClient")
    }
}

    }

    fun getAllPlaylists(): MutableList<Playlist> {

        isYouTubeApiClientNull()
        val list = youTubeApiClient?.getAllPlaylists()
        return list ?: mutableListOf<Playlist>()

    }

    fun logListSongsTitlesOfOnePlaylist() {
        MainScope().launch(Dispatchers.IO) {
            isYouTubeApiClientNull()
            val list = youTubeApiClient?.getAllPlaylists()
            val firstPlaylist = list?.get(1)
            val myPlaylistsTitlesAndIDsString =
                getStringFromShared(Constants.DATA_PLAYLISTS_TITLES_AND_IDS)
            myPlaylistsTitlesAndIDs =
                myPlaylistsTitlesAndIDsString?.let { deserializeStringToMap(it) }
            val mustBeId = myPlaylistsTitlesAndIDs?.get("1")
            Log.d(
                TAG,
                "logListSongsTitlesOfOnePlaylist: ${mustBeId == firstPlaylist?.id} $mustBeId ${firstPlaylist?.id} ${firstPlaylist?.snippet?.title}"
            )

            val listOfSongs =
                firstPlaylist?.id?.let { youTubeApiClient?.getAllSongsTitlesFromPlaylist(it) }
            Log.d(TAG, "list $listOfSongs")
            launch(Dispatchers.Main) {
                listOfSongs?.forEach { Log.d("ttt", "- $it") }
            }
        }
    }

    fun saveMyPlaylists(waiting: ProgressDialog) {
        MainScope().launch(Dispatchers.IO) {
            isYouTubeApiClientNull()
            val list = youTubeApiClient?.getAllPlaylists()
            list?.forEach { playlist ->
                val playlistId = playlist.id
                val playlistTitle = playlist.snippet.title

                Log.d(TAG, "saveMyPlaylists: playlistId playlistTitle $playlistId $playlistTitle")

                myPlaylistsTitles?.add(playlistTitle)
                myPlaylistsTitlesAndIDs?.put(playlistTitle, playlistId)

                val listSongsID = youTubeApiClient?.getAllSongsIDFromPlaylist(playlistId)
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
                waiting.cancel()
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

        isYouTubeApiClientNull()
        myPlaylistsTitles?.forEach { title ->
            val playlistId = myPlaylistsTitlesAndIDs?.get(title)
            Log.d(TAG, "restoreMyPlaylists: playlistId - $playlistId")
            val songsIDsListMustBeString = getStringFromShared(title)
            val songsIDsListMustBe =
                songsIDsListMustBeString?.let { deserializeStringToList(it) }

            val songsIDsListFromAPI = playlistId?.let {
                youTubeApiClient?.getAllSongsIDFromPlaylist(it)
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

    suspend fun getListUniqueVideosFromAllPlaylists(): MutableList<String> {
        return with(Dispatchers.IO) {
            isYouTubeApiClientNull()
            val listUniqueVideosIDs = mutableListOf<String>()
            val playlists = youTubeApiClient?.getAllPlaylists()
            playlists?.forEach { playlist ->
                val playlistID = playlist.id
                val playlistsVideosIDs = youTubeApiClient?.getAllSongsIDFromPlaylist(playlistID)
                playlistsVideosIDs?.let { list ->
                    list.forEach { videoID ->
                        if (!listUniqueVideosIDs.contains(videoID)) {
                            listUniqueVideosIDs.add(videoID)
                        }
                    }
                }
            }
            listUniqueVideosIDs
        }
    }

    suspend fun getListUniqueVideosFromGivenPlaylists(listPlaylistsIDs :MutableList<String>): MutableList<String> {
        return with(Dispatchers.IO) {
            isYouTubeApiClientNull()
            val listUniqueVideosIDs = mutableListOf<String>()
            listPlaylistsIDs.forEach { playlistID ->
                val playlistsVideosIDs = youTubeApiClient?.getAllSongsIDFromPlaylist(playlistID)
                playlistsVideosIDs?.let { list ->
                    list.forEach { videoID ->
                        if (!listUniqueVideosIDs.contains(videoID)) {
                            listUniqueVideosIDs.add(videoID)
                        }
                    }
                }
            }
            listUniqueVideosIDs
        }
    }

    fun deleteDuplicates(playlistsID: MutableList<String>) {

        isYouTubeApiClientNull()
        val listUniqueVideosIDs = mutableListOf<String>()
        var playlists: MutableList<Playlist>? = mutableListOf()
        if (playlistsID.isEmpty()) {
            playlists = youTubeApiClient?.getAllPlaylists()
        } else {
            playlistsID.forEach { id ->
                val playlist = youTubeApiClient?.getPlaylistById(id)
                playlist?.let { playlists?.add(it) }
            }
        }

        playlists?.forEach { playlist ->
            val playlistID = playlist.id
            val playlistsVideosIDs = youTubeApiClient?.getAllSongsIDFromPlaylist(playlistID)
            playlistsVideosIDs?.let { list ->
                list.forEach { videoID ->
                    // якщо не містить
                    if (!listUniqueVideosIDs.contains(videoID)) {
                        listUniqueVideosIDs.add(videoID)
                    } else {
                        val playlistItemID =
                            youTubeApiClient?.findPlaylistItemId(playlistID, videoID)
                        playlistItemID?.let { youTubeApiClient?.deleteVideoFromPlaylist(it) }
                    }
                }
            }
        }
    }

    suspend fun getVideoInfo(videoID: String): VideoInfo? {
        return with(Dispatchers.IO) {
            isYouTubeApiClientNull()
            youTubeApiClient?.getVideoInfo(videoID)
        }
    }

    //
//
//
//
//
// допоміжні методи
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