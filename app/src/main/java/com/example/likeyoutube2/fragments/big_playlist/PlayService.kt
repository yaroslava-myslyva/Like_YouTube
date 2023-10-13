package com.example.likeyoutube2.fragments.big_playlist


import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.util.SparseArray
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.likeyoutube2.MainActivity.Companion.TAG
import com.example.likeyoutube2.R
import com.example.likeyoutube2.databinding.FragmentBigPlaylistBinding
import com.example.likeyoutube2.from_libs.*
import com.example.likeyoutube2.internet.WorkerWithApiClient
import com.example.likeyoutube2.randomizer.VideoIdAndTime
import com.example.likeyoutube2.randomizer.WorkerBigPlaylist
import com.google.android.exoplayer2.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class PlayService : Service() {
    val mediaPlayer: MediaPlayer = MediaPlayer()
    private val binder = LocalBinder()
    private val workerWithApiClient = WorkerWithApiClient()
    lateinit var bigList: MutableLiveData<MutableList<VideoIdAndTime>>
    private lateinit var workerBigPlaylist: WorkerBigPlaylist
    private lateinit var bpfBinding: FragmentBigPlaylistBinding
    private lateinit var fragment: BigPlaylistFragment
    private var isException = false

    inner class LocalBinder : Binder() {
        fun getService(): PlayService = this@PlayService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    fun initService(
        bigList: MutableList<VideoIdAndTime>,
        workerBigPlaylist: WorkerBigPlaylist,
        bpfBinding: FragmentBigPlaylistBinding, fragment: BigPlaylistFragment
    ) {
        this.bigList = MutableLiveData(bigList)
        this.workerBigPlaylist = workerBigPlaylist
        this.bpfBinding = bpfBinding
        this.fragment = fragment
    }

    fun playing() {
        Log.d(TAG, "playing: ")
        bpfBinding.buttonPlay.visibility = View.GONE
        bpfBinding.progressBar.visibility = View.VISIBLE

        val first = bigList.value?.first()
        if (first != null) {
            connectSong(first)
        }
        mediaPlayer.setOnCompletionListener {
            if (!isException) {
                bpfBinding.buttonPlay.visibility = View.GONE
                bpfBinding.progressBar.visibility = View.VISIBLE
                bpfBinding.buttonPlay.setImageResource(R.drawable.baseline_play_arrow_24)
                bigList.value =
                    bigList.value?.let { it1 -> workerBigPlaylist.movingFirstToEnd(it1) }
                connectSong(bigList.value!!.first())
            } else {
                Log.d(TAG, "playing: isException $isException")
                isException = false
            }
        }

        mediaPlayer.setOnPreparedListener {
            bpfBinding.buttonPlay.setImageResource(R.drawable.round_pause_24)
        }
    }

    private fun connectSong(idAndTime: VideoIdAndTime) {
        Log.d(TAG, "connectSong: ")
        MainScope().launch(Dispatchers.IO) {
            val videoInfo = workerWithApiClient.getVideoInfo(idAndTime.videoID)
            launch(Dispatchers.Main) {
                bpfBinding.title.text = videoInfo?.videoTitle
                val picture = videoInfo?.pictureUrl
                fragment.view?.let {
                    Glide.with(it)
                        .load(picture)
                        .transition(DrawableTransitionOptions.withCrossFade(1500))
                        .into(bpfBinding.image)
                }
                youTubeExtractorAndPlayer(videoInfo, idAndTime)
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    fun youTubeExtractorAndPlayer(videoInfo: VideoInfo?, idAndTime: VideoIdAndTime) {
        object : YouTubeExtractor(fragment.mainActivity) {
            override fun onExtractionComplete(
                ytFiles: SparseArray<YtFile>?,
                vMeta: VideoMeta?
            ) {
                if (ytFiles != null) {
                    val itag = ytFiles.keyAt(0)
                    val downloadUrl: String = ytFiles[itag].getUrl()
                    val videoUri = Uri.parse(downloadUrl)
                    playVideo(videoUri, idAndTime)
                }
            }
        }.extract(videoInfo?.videoUrl)
    }


    fun playVideo(videoUri: Uri, idAndTime: VideoIdAndTime) {
        try {
            bpfBinding.buttonPlay.visibility = View.GONE
            bpfBinding.progressBar.visibility = View.VISIBLE
            mediaPlayer.reset()
            Log.d(TAG, "playVideo: prepare")
            mediaPlayer.setDataSource(applicationContext, videoUri)
            mediaPlayer.prepare()
            Log.d(TAG, "playVideo: finish prepare")
            mediaPlayer.start()
            bpfBinding.buttonPlay.visibility = View.VISIBLE
            bpfBinding.progressBar.visibility = View.GONE
            bpfBinding.buttonPlay.setImageResource(R.drawable.baseline_pause_24)
            val localTimeZone = TimeZone.getDefault()
            val calendar = Calendar.getInstance(localTimeZone)
            val currentDate = calendar.time
            val newIdAndTime = VideoIdAndTime(idAndTime.videoID, currentDate)
            bigList.value?.set(0, newIdAndTime)
            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
            Log.d(TAG, "playVideo: currentDate ${dateFormat.format(newIdAndTime.lastListening)}")

        } catch (e: Exception) {
            isException = true
            nextVideo()
            e.printStackTrace()
        }
    }

    fun pauseVideo() {
        Log.d(TAG, "pauseVideo: ")
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            bpfBinding.buttonPlay.setImageResource(R.drawable.baseline_play_arrow_24)
        }
    }

    fun playAfterPause() {
        Log.d(TAG, "playAfterPause: ")
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            bpfBinding.buttonPlay.setImageResource(R.drawable.baseline_pause_24)
        }
    }

    fun nextVideo() {
        Log.d(TAG, "nextVideo: ")
        mediaPlayer.pause()
        bpfBinding.buttonPlay.visibility = View.GONE
        bpfBinding.progressBar.visibility = View.VISIBLE
        bigList.value = bigList.value?.let { workerBigPlaylist.movingFirstToEnd(it) }
        bigList.value?.first()?.let { connectSong(it) }
    }

    fun previousVideo() {
        Log.d(TAG, "previousVideo: ")
        mediaPlayer.pause()
        bpfBinding.buttonPlay.visibility = View.GONE
        bpfBinding.progressBar.visibility = View.VISIBLE
        bigList.value = bigList.value?.let { workerBigPlaylist.movingLastInFront(it) }
        bigList.value?.first()?.let { connectSong(it) }
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

//    private fun extractAudioStreamUrl(videoPage: String): String? {
//        val pattern: Pattern = Pattern.compile("\"url\":\"([^\"]*)\"")
//        val matcher: Matcher = pattern.matcher(videoPage)
//        return if (matcher.find()) {
//            matcher.group(1)
//        } else null
//    }
//
//    suspend fun getYtAudioUrl(id: String): String {
//        return withContext(Dispatchers.IO) {
//            val client = OkHttpClient()
//
//            val request = Request.Builder()
//                .url(id)
//                .build()
//
//            try {
//                val response = client.newCall(request).execute()
//                Log.d(TAG, "getYtAudioUrl: ${response.body?.contentType()}")
//                if (!response.isSuccessful) {
//                    throw IOException("Unexpected response code: ${response.code}")
//                }
//
//                val data = response.body?.string() ?: ""
//                Log.d(TAG, "getYtAudioUrl: data $data")
//
//                val dat = data.split("&")
//                    .filter { it.isNotBlank() }
//                    .associate {
//                        val keyValue = it.split("=")
//                        Pair(
//                            keyValue[0], //URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name())
//                            URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name())
//                        )
//                    }
//
//                if (dat.containsKey("reason")) {
//                    throw Exception(dat["reason"])
//                }
//
//                val fmtss = dat["adaptive_fmts"]
//                val fmts = fmtss?.split(",")?.filter { it.isNotBlank() }?.map {
//                    it.split("&").filter { it.isNotBlank() }
//                        .associate {
//                            val keyValue = it.split("=")
//                            Pair(
//                                keyValue[0],
//                                URLDecoder.decode(
//                                    keyValue[1],
//                                    StandardCharsets.UTF_8.name()
//                                )
//                            )
//                        }
//                } ?: emptyList()
//
//                val fmt = fmts.asSequence()
//                    .filter { it["type"]?.startsWith("audio/mp4") == true }
//                    .maxByOrNull { it["bitrate"]?.toIntOrNull() ?: 0 }
//
//                if (fmt == null) {
//                    throw IllegalStateException("The audio stream did not contain suitable formats.")
//                }
//
//                var url = fmt["url"] ?: ""
//
//                if (fmt.containsKey("s")) {
//                    val sig = fmt["s"] ?: ""
//                    val videoPage = client.newCall(
//                        Request.Builder()
//                            .url("https://www.youtube.com/watch?v=$id")
//                            .build()
//                    ).execute()
//
//                    if (!videoPage.isSuccessful) {
//                        throw IOException("Unexpected response code: ${videoPage.code}")
//                    }
//
//                    val jsMatch = Regex(""""js":"(.+?)""").find(videoPage.body?.string() ?: "")
//                    val jsUrl = jsMatch?.groupValues?.get(1)?.let { "https:$it" } ?: ""
//
//                    val jsResponse = client.newCall(Request.Builder().url(jsUrl).build()).execute()
//
//                    if (!jsResponse.isSuccessful) {
//                        throw IOException("Unexpected response code: ${jsResponse.code}")
//                    }
//
//                    val jsContent = jsResponse.body?.string() ?: ""
//                    val jsFuncMatch =
//                        Regex("""\.(set\("signature",([a-zA-Z]+?)\(.+?\)\);)""").find(jsContent)
//                    val jsFunction = jsFuncMatch?.groupValues?.get(1) ?: ""
//
//                    val jsArgsMatch =
//                        Regex("$jsFunction=function\\(([a-zA-Z]+?)\\)\\{(.+?)\\};").find(jsContent)
//                    val jsArgs = jsArgsMatch?.groupValues?.get(1) ?: ""
//                    val jsAlgo = jsArgsMatch?.groupValues?.get(2) ?: ""
//
//                    val jsFunc = "var unscramble = function($jsArgs) { $jsAlgo };"
//                    val jsCode = "$jsAlgo\n$jsFunc\nunscramble('$sig');"
//
//                    val engine = Engine.create()
//                    val context: Context = Context.create()
//                    val evalResult = context.eval("js", jsCode)
//
//                    val unscrambledSig = evalResult.toString()
//
//                    url = "$url&signature=$unscrambledSig"
//                }
//
//                url
//            } catch (e: Exception) {
//                throw e
//            }
//        }
//
//    }
//
//    private fun extractAudioURL(id: String): String {
//        //Log.i(LOG_TAG, "START extract audio");
//        val request: YoutubeDLRequest = YoutubeDLRequest("https://youtu.be/xa0zf8CxXcc");
//        request.setOption("--extract-audio");
//        Log.d(TAG, "extractAudioURL: ${request}")
//        val streamInfo = YoutubeDL.getVideoInfo(request.url);
//        //  Log.i(LOG_TAG, "FINISH extract audio");
//        return streamInfo.playerUrl;
//    }

    //           val  exoPlayer = SimpleExoPlayer.Builder(fragment.mainActivity, DefaultRenderersFactory(fragment.mainActivity)).build() //
//             //ExoPlayer.Builder(fragment.mainActivity, ExtractorsFactory()).build()
//            Log.d(TAG, "playVideo: 1")
//            exoPlayer?.playWhenReady = true
//            Log.d(TAG, "playVideo: 2")
//           // binding.playerView.player = exoPlayer
//            val defaultHttpDataSourceFactory = DefaultHttpDataSource.Factory()
//            Log.d(TAG, "playVideo: 3")
////            defaultHttpDataSourceFactory.defaultRequestProperties.snapshot.forEach {
////                Log.d(TAG, "playVideo: ${it.key} ${it.value}")
////            }
//            Log.d(TAG, "playVideo: 4")
//            val mediaItem =
//                MediaItem.fromUri(videoUri)
//            Log.d(TAG, "playVideo: 5")
//            val mediaSource =
//                HlsMediaSource.Factory(defaultHttpDataSourceFactory).createMediaSource(mediaItem)
//            Log.d(TAG, "playVideo: 6")
//            exoPlayer?.setMediaSource(mediaSource)
//           // exoPlayer?.seekTo(playbackPosition)
//            exoPlayer?.playWhenReady = true
//            exoPlayer?.prepare()
}


