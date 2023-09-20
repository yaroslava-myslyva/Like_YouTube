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
import com.example.likeyoutube.randomizer.RandomizerWithPriorities
import com.example.likeyoutube.randomizer.VideoIdAndPriority
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


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
        val list = mutableListOf<VideoIdAndPriority>(
            VideoIdAndPriority("id1", 3),
            VideoIdAndPriority("id2", 3),
            VideoIdAndPriority("id3", 3)
        )
        val rand = RandomizerWithPriorities()
        val result = rand.randomize(list)


    }
}
