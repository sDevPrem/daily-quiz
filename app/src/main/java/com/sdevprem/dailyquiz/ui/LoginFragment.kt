package com.sdevprem.dailyquiz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.sdevprem.dailyquiz.R
import com.sdevprem.dailyquiz.databinding.FragmentLoginBinding

class LoginFragment : Fragment(){
    lateinit var binding : FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentLoginBinding
            .inflate(
                inflater,
                container,
                false
            ).apply { binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSignUp.setOnClickListener {
            findNavController().navigate(
                LoginFragmentDirections
                    .actionLoginFragmentToSignUpFragment()
            )
        }
        binding.btnLogin.setOnClickListener{
            findNavController().navigate(
                LoginFragmentDirections
                    .actionLoginFragmentToHomeFragment()
            )
        }
    }
}