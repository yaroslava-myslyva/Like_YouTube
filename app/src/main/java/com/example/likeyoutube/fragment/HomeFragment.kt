package com.example.likeyoutube.fragment

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.likeyoutube.Constants
import com.example.likeyoutube.MainActivity
import com.example.likeyoutube.MainActivity.Companion.TAG
import com.example.likeyoutube.R
import com.example.likeyoutube.databinding.FragmentHomeBinding
import com.example.likeyoutube.internet.WorkerWithApiClient
import com.example.likeyoutube.internet.AuthenticationImplementer
import com.example.likeyoutube.randomizer.PlaylistsWorker
import com.example.likeyoutube.randomizer.VideoIdAndTime
import com.google.api.services.youtube.model.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import kotlin.concurrent.thread


class HomeFragment : Fragment() {

    private lateinit var fragmentHomeBinding: FragmentHomeBinding
    private val authenticationImplementer = AuthenticationImplementer.getInctance()
    private val workerWithApiClient = WorkerWithApiClient()
    private lateinit var mainActivity: MainActivity
    private var list: MutableList<Playlist> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        fragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return fragmentHomeBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity = activity as MainActivity
        val waiting = ProgressDialog(context)
        waiting.show()

        val firstName = mainActivity.getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
        ).getString(Constants.DATA_FIRST_NAME, "")

        val urlPicture = mainActivity.getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
        ).getString(Constants.DATA_PICTURE, "")

        mainActivity.activityMainBinding.userName.text = firstName
        MainScope().launch(Dispatchers.Main) {
            Glide.with(mainActivity)
                .load(urlPicture)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade(1500))
                .into(mainActivity.activityMainBinding.userProfileImage)

        }
        setRecyclerView(waiting)

        with(fragmentHomeBinding) {
            buttonSaveMyPlaylists.setOnClickListener {
                waiting.show()
                workerWithApiClient.saveMyPlaylists(waiting)
            }
            buttonDeleteDuplicates.setOnClickListener {
                waiting.show()
                deleteDuplicates(waiting)
            }
            buttonRestoreMyPlaylists.setOnClickListener {
                waiting.show()
                thread(start = true) {
                    workerWithApiClient.restoreMyPlaylists()
                    waiting.cancel()
                }
            }
        }

        val popupMenu = PopupMenu(mainActivity, mainActivity.activityMainBinding.userProfileImage)
        popupMenu.menuInflater.inflate(R.menu.from_user_url, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                authenticationImplementer.signOutWithoutRedirect()
                return true
            }
        })
        mainActivity.activityMainBinding.userProfileImage.setOnClickListener { popupMenu.show() }


        //  tsiatsia()
    }

    private fun setRecyclerView(waiting: ProgressDialog) {
        MainScope().launch(Dispatchers.IO) {
            list = workerWithApiClient.getAllPlaylists()
            launch(Dispatchers.Main) {
                val playlistsAdapter = PlaylistsAdapter()
                playlistsAdapter.setList(list)
                with(fragmentHomeBinding.recyclerPlaylists) {
                    adapter = playlistsAdapter
                    layoutManager = LinearLayoutManager(context)
                }
                waiting.cancel()
            }
        }

    }

    private fun deleteDuplicates(waiting: ProgressDialog) {
        with(fragmentHomeBinding.recyclerPlaylists) {
            MainScope().launch(Dispatchers.Main) {
                val playlistsID = mutableListOf<String>()
                for (i in 0 until childCount) {
                    val view = getChildAt(i)
                    val checkBox = view.findViewById<CheckBox>(R.id.item_playlists_check_box)

                    if (checkBox.isChecked) {
                        val selectedItemID = list[i].id
                        playlistsID.add(selectedItemID)
                    }
                }
                launch(Dispatchers.IO) {
                    workerWithApiClient.deleteDuplicates(playlistsID)
                    launch(Dispatchers.Main) {
                        for (i in 0 until childCount) {
                            val view = getChildAt(i)
                            val checkBox =
                                view.findViewById<CheckBox>(R.id.item_playlists_check_box)
                            checkBox.isChecked = false
                        }
                        waiting.cancel()
                    }
                }
            }
        }
    }


    private fun tsiatsia() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        dateFormat.parse("2023-04-05")
        val list = mutableListOf<VideoIdAndTime>(
            VideoIdAndTime("1", dateFormat.parse("2023-01-05")),
            VideoIdAndTime("2"),
            VideoIdAndTime("3"),
            VideoIdAndTime("4"),
            VideoIdAndTime("5"),
            VideoIdAndTime("6"),
            VideoIdAndTime("7"),
            VideoIdAndTime("8"),
            VideoIdAndTime("9"),
            VideoIdAndTime("10"),
            VideoIdAndTime("11"),
            VideoIdAndTime("12"),
            VideoIdAndTime("13"),
            VideoIdAndTime("14"),
            VideoIdAndTime("15"),
            VideoIdAndTime("16", dateFormat.parse("2023-02-05")),
            VideoIdAndTime("17", dateFormat.parse("2023-03-05")),
            VideoIdAndTime("18", dateFormat.parse("2023-04-05")),
        )
        val playlistsWorker = PlaylistsWorker()
        playlistsWorker.setActivity(mainActivity)
        val result = playlistsWorker.randomize(list)
        Log.d(TAG, "tsiatsia: size ${list.size} ${result.size}")
        result.forEach {
            Log.d(TAG, "tsiatsia: ${it.videoID}")
        }

    }
}
