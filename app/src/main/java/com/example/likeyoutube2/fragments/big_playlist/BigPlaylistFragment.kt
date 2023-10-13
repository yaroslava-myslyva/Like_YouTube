package com.example.likeyoutube2.fragments.big_playlist


import android.app.ProgressDialog
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.likeyoutube2.MainActivity
import com.example.likeyoutube2.MainActivity.Companion.TAG
import com.example.likeyoutube2.databinding.FragmentBigPlaylistBinding
import com.example.likeyoutube2.randomizer.WorkerBigPlaylist
import com.example.likeyoutube2.randomizer.VideoIdAndTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BigPlaylistFragment : Fragment() {
    lateinit var mainActivity: MainActivity
    private lateinit var binding: FragmentBigPlaylistBinding
    val workerBigPlaylist = WorkerBigPlaylist()
    var list = mutableListOf<VideoIdAndTime>()
    private lateinit var playService: PlayService
    private lateinit var waiting: ProgressDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBigPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        waiting = ProgressDialog(context)
        waiting.show()
        mainActivity = activity as MainActivity
        workerBigPlaylist.setActivity(mainActivity)
        list = workerBigPlaylist.getBigPlaylistFromShared() ?: mutableListOf()
        Log.d(TAG, "bid fragment onActivityCreated: size = ${list.size}")
        setActionBar()
        val serviceIntent = Intent(mainActivity, PlayService::class.java)
        mainActivity.bindService(
            serviceIntent,
            serviceConnection,
            android.content.Context.BIND_AUTO_CREATE
        )


        with(binding) {
            buttonPlay.setOnClickListener {
                if (playService.mediaPlayer.isPlaying) {
                    playService.pauseVideo()
                } else {
                    playService.playAfterPause()
                }
            }
            buttonNext.setOnClickListener {
                playService.nextVideo()
            }
            buttonPrevious.setOnClickListener {
                playService.previousVideo()
            }

        }

    }


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected: ")
            val binder = service as PlayService.LocalBinder
            playService = binder.getService()
            playService.initService(list, workerBigPlaylist, binding, this@BigPlaylistFragment)
            playService.playing()
            playService.bigList.observe(mainActivity) {
                Log.d(TAG, "onServiceConnected: observe")
                workerBigPlaylist.saveBigPlaylist(it)
                setRecyclerView(waiting)

            }

            binding.buttonDecPriority.setOnClickListener {
                playService.nextVideo()
                val currentList = playService.bigList.value
                val newList = currentList?.let { cl ->
                    workerBigPlaylist.decreasingPriority(cl.last(), cl)
                }
                if (newList != null) {
                    list = newList
                }
                playService.bigList.value = newList
            }

            binding.buttonRandomize.setOnClickListener {
                val currentList = playService.bigList.value
                val newList = currentList?.let { cl ->
                    workerBigPlaylist.randomize(cl)
                }
                if (newList != null) {
                    list = newList
                }
                playService.bigList.value = newList
            }

            setRecyclerView(waiting)

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Обробка відключення служби (не обов'язково)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity.unbindService(serviceConnection)
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
                list = playService.bigList.value ?: mutableListOf()
                val bigPlaylistAdapter = BigPlaylistAdapter()
                bigPlaylistAdapter.setList(list)
                bigPlaylistAdapter.setMainActivity(mainActivity)
                Log.d(TAG, "setRecyclerView: list.size ${list.size}")
                with(binding.recyclerBigPlaylist) {
                    adapter = bigPlaylistAdapter
                    layoutManager = LinearLayoutManager(context)
                }
                waiting.cancel()
            }
        }
    }
}

