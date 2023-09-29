package com.example.likeyoutube.fragments.big_playlist

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.likeyoutube.MainActivity
import com.example.likeyoutube.MainActivity.Companion.TAG
import com.example.likeyoutube.databinding.ItemBigPlaylistBinding
import com.example.likeyoutube.fragments.VideoFragment
import com.example.likeyoutube.internet.WorkerWithApiClient
import com.example.likeyoutube.randomizer.VideoIdAndTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class BigPlaylistAdapter : RecyclerView.Adapter<BigPlaylistAdapter.BigPlaylistViewHolder>() {
    private lateinit var mainActivity: MainActivity
    private lateinit var list: List<VideoIdAndTime>
    private val workerWithApiClient = WorkerWithApiClient()

    fun setList(list: List<VideoIdAndTime>) {
        this.list = list
    }

    fun setMainActivity(activity: MainActivity){
        mainActivity = activity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BigPlaylistViewHolder {

        val binding = ItemBigPlaylistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return BigPlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BigPlaylistViewHolder, position: Int) {
        val videoIdAndTime = list[position]
        holder.bind(videoIdAndTime)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class BigPlaylistViewHolder(private val binding: ItemBigPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(videoIdAndTime: VideoIdAndTime) {
            Log.d(TAG, "bind: ")
            MainScope().launch (Dispatchers.IO){
                val videoInfo = workerWithApiClient.getVideoInfo(videoIdAndTime.videoID)
                launch(Dispatchers.Main) {
                    val picture = videoInfo?.pictureUrl
                    binding.title.text = videoInfo?.videoTitle
                    Glide.with(itemView)
                        .load(picture)
                        .transition(DrawableTransitionOptions.withCrossFade(1500))
                        .into(binding.picture)
                    itemView.setOnClickListener {

//                        val intentStartYoutube = YouTubeIntents.createPlayVideoIntent( mainActivity.baseContext
//                            , videoIdAndTime.videoID
                        //)ApplicationProvider.getApplicationContext<Context>()
                        // Запускаем приложение YouTube с указанным видеороликом
                        // Запускаем приложение YouTube с указанным видеороликом
                       // mainActivity.startActivity(intentStartYoutube)
                        val videoFragment = VideoFragment()
                        videoInfo?.videoUrl?.let { videoUrl -> videoFragment.setVideoUrl(videoUrl) }
                        mainActivity.supportFragmentManager.beginTransaction()
                            .replace(mainActivity.activityMainBinding.fragment.id, videoFragment)
                            .addToBackStack(null)
                            .commit()
                    }


                }
            }

        }
    }
}