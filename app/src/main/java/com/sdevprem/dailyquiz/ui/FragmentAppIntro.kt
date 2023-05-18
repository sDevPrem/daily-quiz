package com.sdevprem.dailyquiz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sdevprem.dailyquiz.R
import com.sdevprem.dailyquiz.data.UserRepository
import com.sdevprem.dailyquiz.databinding.FragmentAppIntroBinding
import com.sdevprem.dailyquiz.databinding.FragmentAppIntroBindingImpl
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FragmentAppIntro : Fragment(R.layout.fragment_app_intro) {
    lateinit var binding : FragmentAppIntroBinding
    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentAppIntroBinding.inflate(
            inflater,
            container,
            false
        ).apply { binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch{
            if(userRepository.isUserSignIn()){
                findNavController().navigate(
                    FragmentAppIntroDirections
                        .actionAppIntroFragmentToHomeFragment()
                )

            }else{
                binding.progressBar.isVisible = false
                binding.btnGetStarted.apply {
                    isVisible = true
                    setOnClickListener{
                        findNavController().navigate(
                            FragmentAppIntroDirections
                                .actionAppIntroFragmentToLoginFragment()
                        )
                    }
                }
            }
        }
    }
}