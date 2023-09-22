package com.example.likeyoutube.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.likeyoutube.Constants
import com.example.likeyoutube.MainActivity
import com.example.likeyoutube.MainActivity.Companion.TAG
import com.example.likeyoutube.R
import com.example.likeyoutube.internet.WorkerWithApiClient
import com.example.likeyoutube.databinding.FragmentHomeBinding
import com.example.likeyoutube.internet.AuthenticationImplementer
import com.example.likeyoutube.randomizer.PlaylistsWorker
import com.example.likeyoutube.randomizer.VideoIdAndTime
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat


class HomeFragment : Fragment() {

    private lateinit var fragmentHomeBinding: FragmentHomeBinding
    private val authenticationImplementer = AuthenticationImplementer.getInctance()
    private val workerWithApiClient = WorkerWithApiClient()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        fragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return fragmentHomeBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mainActivity = activity as MainActivity
        val firstName = mainActivity.getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
        ).getString(Constants.DATA_FIRST_NAME, "")

        val urlPicture = mainActivity.getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
        ).getString(Constants.DATA_PICTURE, "")

        mainActivity.activityMainBinding.userName.text = firstName

        Glide.with(mainActivity)
            .load(urlPicture)
            .circleCrop()
            .transition(DrawableTransitionOptions.withCrossFade(1500))
            .into(mainActivity.activityMainBinding.userProfileImage)


        with(fragmentHomeBinding) {
            buttonSaveMyPlaylists.setOnClickListener {
                workerWithApiClient.saveMyPlaylists()
            }
            buttonUniqueVideos.setOnClickListener {
                MainScope().launch {
                    workerWithApiClient.getListUniqueVideos()
                }
            }
            buttonDeleteDuplicates.setOnClickListener {
                workerWithApiClient.deleteDuplicates()
            }
            buttonRestoreMyPlaylists.setOnClickListener {
                workerWithApiClient.restoreMyPlaylists()
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


        tsiatsia()
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
        val result = playlistsWorker.randomize(list)
        Log.d(TAG, "tsiatsia: size ${list.size} ${result.size}")
        result.forEach {
            Log.d(TAG, "tsiatsia: ${it.videoID}")
        }

    }
}
