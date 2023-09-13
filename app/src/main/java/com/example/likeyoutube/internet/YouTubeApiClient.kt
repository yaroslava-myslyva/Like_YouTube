package com.example.likeyoutube.internet

import android.content.Context
import com.example.likeyoutube.R
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.Playlist

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
    fun getPlaylists(): MutableList<Playlist>? {
        val request = mYouTube.playlists().list("snippet")
        request.mine = true // This fetches the playlists for the authorized user.

        val response = request.execute()
        return response.items
    }
}