package com.sdevprem.dailyquiz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.sdevprem.dailyquiz.R
import com.sdevprem.dailyquiz.databinding.FragmentSignUpBinding
import kotlinx.coroutines.launch

class SignUpFragment : Fragment(R.layout.fragment_sign_up){
    lateinit var binding : FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentSignUpBinding.inflate(
            inflater,
            container,
            false
        ).apply {
            binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSignUp.setOnClickListener{
            findNavController().navigate(
                SignUpFragmentDirections
                    .actionSignUpFragmentToHomeFragment()
            )
        }
        binding.btnLogIn.setOnClickListener {
            findNavController().navigate(
                SignUpFragmentDirections
                    .actionSignUpFragmentToLoginFragment()
            )
        }

    }
}