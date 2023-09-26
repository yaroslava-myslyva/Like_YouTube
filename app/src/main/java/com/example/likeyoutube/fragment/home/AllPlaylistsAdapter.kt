package com.example.likeyoutube.fragment.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.likeyoutube.databinding.ItemAllPlaylistsBinding
import com.google.api.services.youtube.model.Playlist

class AllPlaylistsAdapter : RecyclerView.Adapter<AllPlaylistsAdapter.PlaylistViewHolder>() {

    private lateinit var list: List<Playlist>

    fun setList(list: List<Playlist>) {
        this.list = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {

        val binding = ItemAllPlaylistsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = list[position]
        holder.bind(playlist)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class PlaylistViewHolder(private val binding: ItemAllPlaylistsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            val picture = playlist.snippet.thumbnails.medium.url
            binding.title.text = playlist.snippet.title
            binding.description.text = playlist.snippet.description
            Glide.with(itemView)
                .load(picture)
                .transition(DrawableTransitionOptions.withCrossFade(1500))
                .into(binding.picture)
        }
    }
}