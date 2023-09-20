package com.example.likeyoutube.internet

import android.content.Context
import android.util.Log
import com.example.likeyoutube.MainActivity.Companion.TAG
import com.example.likeyoutube.R
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.*
import java.io.IOException

class YouTubeApiClient(credential: HttpRequestInitializer, context: Context) {
    private var mYouTube: YouTube

    init {
        val httpTransport: HttpTransport = NetHttpTransport()
        mYouTube = YouTube.Builder(
            httpTransport,
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName(context.getString(R.string.app_name))
            .build()
    }

    fun getAllPlaylists(): MutableList<Playlist>? {
        val playlists = mutableListOf<Playlist>()
        var nextPageToken: String? = null

        do {
            val request = mYouTube.playlists().list("snippet")
            request.mine = true
            request.pageToken = nextPageToken

            val response = request.execute()
            val items = response.items

            if (items != null) {
                playlists.addAll(items)
            }

            nextPageToken = response.nextPageToken
        } while (nextPageToken != null)

        return playlists
    }


    fun getSongsTitlesFromPlaylist(playlistId: String): MutableList<String>? {
        val songs = mutableListOf<String>()
        var nextPageToken: String? = null

        do {
            val playlistItemsRequest = mYouTube.playlistItems().list("snippet")
            playlistItemsRequest.playlistId = playlistId
            playlistItemsRequest.pageToken = nextPageToken

            val playlistItemsResponse = playlistItemsRequest.execute()
            val playlistItems = playlistItemsResponse.items

            if (playlistItems != null) {
                for (playlistItem in playlistItems) {
                    val videoTitle = playlistItem.snippet.title
                    songs.add(videoTitle)
                }
            }

            nextPageToken = playlistItemsResponse.nextPageToken
        } while (nextPageToken != null)

        return songs
    }

    fun getSongsIDFromPlaylist(playlistId: String): MutableList<String>? {
        val ids = mutableListOf<String>()
        var nextPageToken: String? = null

        do {
            val playlistItemsRequest = mYouTube.playlistItems().list("snippet")
            playlistItemsRequest.playlistId = playlistId
            playlistItemsRequest.pageToken = nextPageToken

            val playlistItemsResponse = playlistItemsRequest.execute()
            val playlistItems = playlistItemsResponse.items

            if (playlistItems != null) {
                for (playlistItem in playlistItems) {
                    val videoId = playlistItem.snippet.resourceId.videoId
                    ids.add(videoId)
                }
            }

            nextPageToken = playlistItemsResponse.nextPageToken
        } while (nextPageToken != null)

        return ids
    }

    fun deleteVideoFromPlaylist(playlistItemId: String) {
        val playlistItemsDeleteRequest = mYouTube.playlistItems().delete(playlistItemId)
        playlistItemsDeleteRequest.execute()
    }

    fun addVideoToPlaylist(playlistId: String, videoId: String): Boolean {
        try {
            // Створюємо об'єкт PlaylistItem
            val playlistItem = PlaylistItem()
            val snippet = PlaylistItemSnippet()

            val resourceId = ResourceId()
            resourceId.kind = "youtube#video"
            resourceId.videoId = videoId

            snippet.resourceId = resourceId
            snippet.playlistId = playlistId
            playlistItem.snippet = snippet

            // Викликаємо YouTube Data API для додавання відео до плейлиста
            mYouTube.playlistItems().insert("snippet", playlistItem).execute()

            return true // Відео було успішно додано до плейлиста
        } catch (e: IOException) {
            e.printStackTrace()
            return false // Відео не було додано до плейлиста через помилку
        }

    }

    fun getVideoTitleById(videoId: String): String? {
        try {
            val videoListResponse = mYouTube.videos().list("snippet")
                .setId(videoId)
                .execute()

            val videoList = videoListResponse.items

            if (videoList != null && videoList.isNotEmpty()) {
                val video = videoList[0]
                return video.snippet.title
            }
        } catch (e: GoogleJsonResponseException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }
}