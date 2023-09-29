package com.example.likeyoutube.fragments


import android.R
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.util.forEach
import androidx.fragment.app.Fragment
import com.example.likeyoutube.MainActivity
import com.example.likeyoutube.MainActivity.Companion.TAG
import com.example.likeyoutube.databinding.FragmentVideoBinding
import com.example.likeyoutube.some.VideoMeta
import com.example.likeyoutube.some.YouTubeExtractor
import com.example.likeyoutube.some.YtFile
import com.google.android.youtube.player.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.math.log


class VideoFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var binding: FragmentVideoBinding
    private lateinit var videoUrl: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity = activity as MainActivity
       // var downloadUrl = ""
            val videoView = binding.videoView
            object : YouTubeExtractor(mainActivity) {
                override fun onExtractionComplete(
                    ytFiles: SparseArray<YtFile>?,
                    vMeta: VideoMeta?
                ) {
                    if (ytFiles != null) {
                        val itag = 18
//                        ytFiles.forEach{ i, yt ->
//                            Log.d(TAG, "onExtractionComplete: $i ${yt.url}")
//                        }
                        val downloadUrl: String = ytFiles[itag].getUrl()
                        val videoUri = Uri.parse(downloadUrl)
                        videoView.setVideoURI(videoUri)
                       // videoView.requestFocus();

           videoView.start()

                    }
                }
            }.extract(videoUrl)





//        try {
//
//
//
//
//
//
//            Log.d(TAG, "onActivityCreated: url - ${videoUrl}")
//            videoView.setMediaController(MediaController(context));
//
//        } catch (e: java.lang.Exception) {
//            Log.d(TAG, "onActivityCreated: ${e.message} ${e.javaClass}")
//        }
}
//        val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
//        transaction.replace(R.id.frame_fragment, youTubePlayerFragment)
//        transaction.commit()
//
//        youTubePlayerFragment.initialize(
//            YoutubeDeveloperKey,
//            object : YouTubePlayer.OnInitializedListener {
//                override fun onInitializationSuccess(
//                    arg0: YouTubePlayer.Provider,
//                    youTubePlayer: YouTubePlayer,
//                    b: Boolean
//                ) {
//                    if (!b) {
//                        YPlayer = youTubePlayer
//                        YPlayer.setFullscreen(false)
//                        YPlayer.loadVideo("NMwE93GQcKs")
//                        YPlayer.play()
//                    }
//                }
//
//                override fun onInitializationFailure(
//                    arg0: YouTubePlayer.Provider,
//                    arg1: YouTubeInitializationResult
//                ) {
//                    // TODO Auto-generated method stub
//                }
//            })
//        val youTubePlayerView: YouTubePlayerView = binding.videoPlayer
//        youTubePlayerView.initialize("AIzaSyD6jtc3D7C2joMjRNZIzROehQhHCsNXAcE", object : YouTubePlayer.OnInitializedListener {
//            override fun onInitializationSuccess(
//                provider: YouTubePlayer.Provider?,
//                player: YouTubePlayer?,
//                wasRestored: Boolean
//            ) {
//                if (!wasRestored) {
//                    // Встановлюємо videoID відео, яке ви хочете відтворити
//                    player?.cueVideo(videoUrl)
//                }
//            }
//
//            override fun onInitializationFailure(
//                provider: YouTubePlayer.Provider?,
//                result: YouTubeInitializationResult?
//            ) {
//                // Обробляйте помилки ініціалізації тут, якщо є
//            }
//        })

// Control values
// see more # https://developers.google.com/youtube/player_parameters?hl=en

// Control values
// see more # https://developers.google.com/youtube/player_parameters?hl=en
//        val params = YTParams()
//        val webView = binding.webView
//        webView.settings.javaScriptEnabled = true
//        webView.webViewClient = object : WebViewClient() {
//            override fun onPageFinished(view: WebView?, url: String?) {
//                super.onPageFinished(view, url)
//
//                val playVideoScript = " document.querySelector('video').muted = 0;"
//                webView.evaluateJavascript(playVideoScript, null)
//                //webView.evaluateJavascript(playVideoScript, null)
//                val enableSoundScript = " document.querySelector('video').autoplay = 1; "
//                webView.evaluateJavascript(enableSoundScript, null)
//            }
//     }


// webView.loadUrl("javascript:(function() { var videoElement = document.querySelector('video'); if (videoElement) { videoElement.click(); } })()")
//   webView.loadUrl(videoUrl)
//webView.reload()
//Log.d(TAG, "onActivityCreated: webview ${webView.reload()}")


//        MainScope().launch(Dispatchers.IO) {
//            val videoPage = getVideoPage(videoUrl)
//
//            // Отримуємо URL аудіо-стріму з веб-сторінки відео
//
//            // Отримуємо URL аудіо-стріму з веб-сторінки відео
//            val audioStreamUrl = extractAudioStreamUrl(videoPage!!)
//            val mediaPlayer = MediaPlayer()
//            mediaPlayer.setDataSource(audioStreamUrl);
//
//            // Підготовка і відтворення аудіо
//            mediaPlayer.prepare();
//            mediaPlayer.start()
//        }


@Throws(IOException::class)
private fun getVideoPage(videoUrl: String): String? {
    val pageContent = StringBuilder()
    val url = URL(videoUrl)
    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
    BufferedReader(InputStreamReader(connection.getInputStream())).use { reader ->
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            pageContent.append(line)
        }
    }
    return pageContent.toString()
}

private fun extractAudioStreamUrl(videoPage: String): String? {
    // Ваша логіка парсингу веб-сторінки відео тут
    // Шукайте URL аудіо-стріму в розмітці веб-сторінки і витягніть його.
    // Враховуйте, що цей метод може змінюватися від часу до часу через зміни на YouTube.
    // Використовуйте регулярні вирази або парсери HTML/XML для цього.
    // Ось загальний приклад:
    val pattern: Pattern = Pattern.compile("\"url\":\"([^\"]*)\"")
    val matcher: Matcher = pattern.matcher(videoPage)
    return if (matcher.find()) {
        matcher.group(1)
    } else null
}

fun setVideoUrl(url: String) {
    this.videoUrl = url
}
}