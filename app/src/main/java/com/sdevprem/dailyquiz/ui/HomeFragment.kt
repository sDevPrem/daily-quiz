package com.sdevprem.dailyquiz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sdevprem.dailyquiz.databinding.FragmentHomeBinding

class HomeFragment : Fragment(){
    lateinit var binding : FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentHomeBinding
            .inflate(
                inflater,
                container,
                false
            ).apply { binding = this }
            .root
    }

}