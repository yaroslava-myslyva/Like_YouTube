package com.example.likeyoutube.fragment.one_playlist

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.likeyoutube.MainActivity.Companion.TAG
import com.example.likeyoutube.databinding.ItemBigPlaylistBinding
import com.example.likeyoutube.internet.WorkerWithApiClient
import com.example.likeyoutube.randomizer.VideoIdAndTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BigPlaylistAdapter : RecyclerView.Adapter<BigPlaylistAdapter.BigPlaylistViewHolder>() {

    private lateinit var list: List<VideoIdAndTime>
    private val workerWithApiClient = WorkerWithApiClient()

    fun setList(list: List<VideoIdAndTime>) {
        this.list = list
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
                        //// Встановити URL-адресу відео для відтворення в VideoView
                        //val videoUri = Uri.parse(videoUrl)
                        //videoView.setVideoURI(videoUri)
                        //
                        //// Запустити відтворення відео
                        //videoView.start()
                    }


                }
            }

        }
    }
}