package com.example.likeyoutube.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.likeyoutube.R
import com.example.likeyoutube.databinding.FragmentHomeBinding
import com.example.likeyoutube.databinding.FragmentSignInBinding
import com.example.likeyoutube.internet.AuthenticationImplementer


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val authenticationImplementer = AuthenticationImplementer.getInctance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        binding.buttonLogOut.setOnClickListener {
            authenticationImplementer.signOutWithoutRedirect()
        }

        binding.buttonMakeApiCall.setOnClickListener {
            authenticationImplementer.makeApiCall()
        }
    }


}