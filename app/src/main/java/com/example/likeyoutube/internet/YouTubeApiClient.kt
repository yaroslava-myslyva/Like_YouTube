package com.example.likeyoutube.internet

import android.content.Context
import com.example.likeyoutube.R
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.Playlist
import com.google.api.services.youtube.model.PlaylistItem
import com.google.api.services.youtube.model.PlaylistItemSnippet
import com.google.api.services.youtube.model.ResourceId

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
                    val videoId = playlistItem.id
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

    fun addVideoToPlaylist(playlistId: String, videoId: String) {
        val playlistItem = PlaylistItem()
        val snippet = PlaylistItemSnippet()
        snippet.playlistId = playlistId
        snippet.resourceId = ResourceId()
        snippet.resourceId.kind = "youtube#video"
        snippet.resourceId.videoId = videoId
        playlistItem.snippet = snippet

        val playlistItemsInsertRequest = mYouTube.playlistItems().insert("snippet", playlistItem)
        val response = playlistItemsInsertRequest.execute()

    }
}