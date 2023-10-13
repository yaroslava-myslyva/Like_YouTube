package com.example.likeyoutube2.fragments.home

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.example.likeyoutube2.MainActivity
import com.example.likeyoutube2.fragments.big_playlist.BigPlaylistFragment
import com.example.likeyoutube2.internet.WorkerWithApiClient
import com.example.likeyoutube2.randomizer.WorkerBigPlaylist
import com.example.likeyoutube2.randomizer.VideoIdAndTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SelectionDialog : DialogFragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var workerWithApiClient: WorkerWithApiClient
    private lateinit var waiting: ProgressDialog
    private lateinit var bigPlaylist: WorkerBigPlaylist

    fun initDialog(
        mainActivity: MainActivity,
        workerWithApiClient: WorkerWithApiClient,
        waiting: ProgressDialog,

        bigPlaylist: WorkerBigPlaylist
    ) {
        this.mainActivity = mainActivity
        this.workerWithApiClient = workerWithApiClient
        this.waiting = waiting
        this.bigPlaylist = bigPlaylist
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = "Selection of playlists"
        val message =
            "You can listen to a video from all your playlists or go back and select the playlists that interest you."
        val button1String = "Return"
        val button2String = "Select all playlists"
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setTitle(title) // заголовок
        builder.setMessage(message) // сообщение
        builder.setPositiveButton(button1String,
            DialogInterface.OnClickListener { dialog, id ->

            })
        // дизайн TODO
        builder.setNegativeButton(button2String,
            DialogInterface.OnClickListener { dialog, id ->
                waiting.show()
                MainScope().launch(Dispatchers.IO) {
                    val listVideoIdAndTime = mutableListOf<VideoIdAndTime>()
                    val listUniqueVideoIDs =
                        workerWithApiClient.getListUniqueVideosFromAllPlaylists()
                    Log.d(MainActivity.TAG, "getListUniqueVideosFromAllPlaylists()")
                    listUniqueVideoIDs.forEach { videoID ->
                        listVideoIdAndTime.add(VideoIdAndTime(videoID))
                    }
                    bigPlaylist.saveBigPlaylist(listVideoIdAndTime)
                    mainActivity.supportFragmentManager.beginTransaction()
                        .replace(
                            mainActivity.activityMainBinding.fragment.id,
                            BigPlaylistFragment()
                        )
                        .addToBackStack(null)
                        .commit()
                    waiting.cancel()
                }
            })
        builder.setCancelable(false)
        return builder.create()
    }
}
