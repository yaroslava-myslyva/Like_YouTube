package com.example.likeyoutube.fragment.one_playlist

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.likeyoutube.MainActivity
import com.example.likeyoutube.MainActivity.Companion.TAG
import com.example.likeyoutube.databinding.FragmentBigPlaylistBinding
import com.example.likeyoutube.fragment.home.AllPlaylistsAdapter
import com.example.likeyoutube.fragment.home.HomeFragment
import com.example.likeyoutube.randomizer.BigPlaylist
import com.example.likeyoutube.randomizer.VideoIdAndTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BigPlaylistFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var binding: FragmentBigPlaylistBinding
    val bigPlaylist = BigPlaylist()
    var list = mutableListOf<VideoIdAndTime>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBigPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val waiting = ProgressDialog(context)
        waiting.show()
        mainActivity = activity as MainActivity
        bigPlaylist.setActivity(mainActivity)
        list = bigPlaylist.getBigPlaylistFromShared() ?: mutableListOf()
        Log.d(TAG, "bid fragment onActivityCreated: size = ${list.size}")
        setActionBar()
        setRecyclerView(waiting)


    }

    override fun onDestroy() {
        super.onDestroy()
        bigPlaylist.saveBigPlaylist(list)
    }

    private fun setActionBar() {

        val toolbar = mainActivity.activityMainBinding.toolbar
        mainActivity.setSupportActionBar(toolbar)
        val actionBar = mainActivity.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = ""

        actionBar?.setHomeButtonEnabled(true)

        toolbar.setNavigationOnClickListener {
            actionBar?.setDisplayHomeAsUpEnabled(false)
            fragmentManager?.popBackStack()

        }
    }

    private fun setRecyclerView(waiting: ProgressDialog) {
        MainScope().launch(Dispatchers.IO) {
            launch(Dispatchers.Main) {
                val playlistsAdapter = BigPlaylistAdapter()
                playlistsAdapter.setList(list)
                Log.d(TAG, "setRecyclerView: list.size ${list.size}")
                with(binding.recyclerBigPlaylist) {
                    adapter = playlistsAdapter
                    layoutManager = LinearLayoutManager(context)
                }
                waiting.cancel()
            }
        }
    }
}