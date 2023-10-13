package com.example.likeyoutube2.fragments.home

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
import com.example.likeyoutube2.Constants
import com.example.likeyoutube2.MainActivity
import com.example.likeyoutube2.MainActivity.Companion.TAG
import com.example.likeyoutube2.R
import com.example.likeyoutube2.databinding.FragmentHomeBinding
import com.example.likeyoutube2.fragments.big_playlist.BigPlaylistFragment
import com.example.likeyoutube2.internet.WorkerWithApiClient
import com.example.likeyoutube2.internet.authentication.GoogleSignInAuthenticationImplementer
import com.example.likeyoutube2.randomizer.WorkerBigPlaylist
import com.example.likeyoutube2.randomizer.VideoIdAndTime
import com.google.api.services.youtube.model.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import kotlin.concurrent.thread


class HomeFragment : Fragment() {

    private lateinit var fragmentHomeBinding: FragmentHomeBinding
    private val authenticationImplementer = GoogleSignInAuthenticationImplementer.getInctance()
    private val workerWithApiClient = WorkerWithApiClient.getInctance()
    private lateinit var mainActivity: MainActivity
    private var list: MutableList<Playlist> = mutableListOf()
    private val bigPlaylist = WorkerBigPlaylist()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        fragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return fragmentHomeBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity = activity as MainActivity
        Log.d(TAG, "HomeFragment start onActivityCreated: ")
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

                thread(start = true) {
                    workerWithApiClient.restoreMyPlaylists()
                    waiting.cancel()
                }
            }
            buttonBigPlaylist.setOnClickListener {
                fetchingBigPlaylist(waiting)
            }

        }

        val popupMenu = PopupMenu(mainActivity, mainActivity.activityMainBinding.userProfileImage)
        popupMenu.menuInflater.inflate(R.menu.from_user_url, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                authenticationImplementer.signOut()
                return true
            }
        })
        mainActivity.activityMainBinding.userProfileImage.setOnClickListener { popupMenu.show() }

    }

    private fun setRecyclerView(waiting: ProgressDialog) {
        MainScope().launch(Dispatchers.IO) {
            list = workerWithApiClient.getAllPlaylists()
            Log.d(TAG, "setRecyclerView: list = ${list.size}")
            launch(Dispatchers.Main) {
                val playlistsAdapter = AllPlaylistsAdapter()
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
            val playlistsID = getCheckedPlaylistsIDs()
            MainScope().launch(Dispatchers.IO) {
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

    private fun fetchingBigPlaylist(waiting: ProgressDialog) {
        bigPlaylist.setActivity(mainActivity)
        val bigList = bigPlaylist.getBigPlaylistFromShared()
        Log.d(TAG, "bigList size - ${bigList?.size}")
        val checkedPlaylists = getCheckedPlaylistsIDs()

        if (checkedPlaylists.isEmpty() && (bigList == null || bigList.isEmpty())) {
            Log.d(TAG, "bigList is empty")
            val dialog = SelectionDialog()
            dialog.initDialog(mainActivity, workerWithApiClient, waiting, bigPlaylist)
            dialog.show(mainActivity.supportFragmentManager, "Selection dialog")

        } else if (checkedPlaylists.isNotEmpty()) {
            waiting.show()
            Log.d(TAG, "checkedPlaylists is not empty")
            MainScope().launch(Dispatchers.IO) {
                val listResultVideoIdAndTime = mutableListOf<VideoIdAndTime>()
                val listUniqueVideoIDs =
                    workerWithApiClient.getListUniqueVideosFromGivenPlaylists(checkedPlaylists)
                listUniqueVideoIDs.forEach { itVideoID ->
                    val foundVIDT =
                        bigList?.find { it.videoID == itVideoID && it.lastListening != null }
                    if (foundVIDT == null) {
                        listResultVideoIdAndTime.add(VideoIdAndTime(itVideoID))
                    } else {
                        listResultVideoIdAndTime.add(foundVIDT)
                        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                        Log.d(
                            TAG,
                            "fetchingBigPlaylist: ${dateFormat.format(foundVIDT.lastListening)}"
                        )
                    }

                }
                bigPlaylist.saveBigPlaylist(listResultVideoIdAndTime)
                mainActivity.supportFragmentManager.beginTransaction()
                    .replace(mainActivity.activityMainBinding.fragment.id, BigPlaylistFragment())
                    .addToBackStack(null)
                    .commit()
                waiting.cancel()
            }
        } else {
            mainActivity.supportFragmentManager.beginTransaction()
                .replace(mainActivity.activityMainBinding.fragment.id, BigPlaylistFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getCheckedPlaylistsIDs(): MutableList<String> {
        val playlistsID = mutableListOf<String>()
        with(fragmentHomeBinding.recyclerPlaylists) {
            for (i in 0 until childCount) {
                val view = getChildAt(i)
                val checkBox = view.findViewById<CheckBox>(R.id.item_playlists_check_box)

                if (checkBox.isChecked) {
                    val selectedItemID = list[i].id
                    playlistsID.add(selectedItemID)
                }
            }
        }
        return playlistsID
    }

}
