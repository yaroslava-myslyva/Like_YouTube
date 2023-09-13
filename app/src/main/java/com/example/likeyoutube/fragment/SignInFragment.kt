package com.example.likeyoutube.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.likeyoutube.R
import com.example.likeyoutube.databinding.FragmentSignInBinding
import com.example.likeyoutube.internet.AuthenticationImplementer


class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    private val authenticationImplementer = AuthenticationImplementer.getInctance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.button.setOnClickListener {
            authenticationImplementer.attemptAuthorization()
        }
    }

}