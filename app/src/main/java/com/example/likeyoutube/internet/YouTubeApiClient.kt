package com.example.likeyoutube.internet

import android.content.Context
import com.example.likeyoutube.R
import com.example.likeyoutube.fragment.one_playlist.VideoInfo
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

    fun getPlaylistById(playlistId: String): Playlist? {
        try {
            val playlistRequest = mYouTube.playlists().list("snippet").setId(playlistId)
            val playlistResponse = playlistRequest.execute()
            val playlists = playlistResponse.items
            if (playlists.isNotEmpty()) {
                return playlists[0]
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getAllSongsTitlesFromPlaylist(playlistId: String): MutableList<String>? {
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

    fun getAllSongsIDFromPlaylist(playlistId: String): MutableList<String>? {
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

    fun findPlaylistItemId(playlistId: String, videoId: String): String? {
        try {
            val playlistItemsResponse = mYouTube.playlistItems().list("id")
                .setPlaylistId(playlistId)
                .setVideoId(videoId)
                .execute()

            if (playlistItemsResponse.items.isNotEmpty()) {
                return playlistItemsResponse.items[0].id
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun deleteVideoFromPlaylist(playlistItemId: String) {
        try {
            mYouTube.playlistItems().delete(playlistItemId).execute()
        } catch (e: IOException) {
            e.printStackTrace()
        }
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

    fun getVideoInfo(videoId: String): VideoInfo? {
        val videoRequest = mYouTube.videos().list("snippet, contentDetails")
        videoRequest.id = videoId

        try {
            val response: VideoListResponse = videoRequest.execute()
            val videos = response.items

            if (videos != null && videos.isNotEmpty()) {
                val video = videos[0]
                val videoUrl = "https://www.youtube.com/watch?v=${video.id}"
                val videoTitle = video.snippet.title
                val thumbnailUrl = video.snippet.thumbnails.medium.url

                return VideoInfo(videoUrl, videoTitle, thumbnailUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}