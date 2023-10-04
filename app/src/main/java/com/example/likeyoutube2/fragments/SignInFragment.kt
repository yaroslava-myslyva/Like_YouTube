package com.example.likeyoutube2.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.likeyoutube2.MainActivity
import com.example.likeyoutube2.R
import com.example.likeyoutube2.databinding.FragmentSignInBinding
import com.example.likeyoutube2.internet.authentication.GoogleSignInAuthenticationImplementer


class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    private val authenticationImplementer = GoogleSignInAuthenticationImplementer.getInctance()
    private lateinit var mainActivity : MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity = activity as MainActivity
        mainActivity.activityMainBinding.userName.text = ""
        mainActivity.activityMainBinding.userProfileImage.setImageResource(R.drawable.ic_baseline_account_circle_24)

        binding.buttonAppAuth.setOnClickListener {
            authenticationImplementer.attemptAuthorization()
        }

        mainActivity.activityMainBinding.userProfileImage.setOnClickListener { }
    }

}