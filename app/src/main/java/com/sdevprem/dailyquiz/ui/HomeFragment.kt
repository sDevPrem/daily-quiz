package com.sdevprem.dailyquiz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.sdevprem.dailyquiz.R
import com.sdevprem.dailyquiz.data.model.Quiz
import com.sdevprem.dailyquiz.databinding.FragmentHomeBinding

class HomeFragment : Fragment(){
    lateinit var binding : FragmentHomeBinding
    private val adapter = QuizAdapter(emptyList())

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val configuration = AppBarConfiguration(
            setOf(R.id.homeFragment),//top level destinations
            binding.drawerLayout
        )
        binding.toolbar.setupWithNavController(findNavController(),configuration)

        populateDummyData()
        binding.quizGrid.layoutManager = GridLayoutManager(requireContext(),2)
        binding.quizGrid.adapter = adapter
    }

    private fun populateDummyData(){
        val dummyList = buildList<Quiz>(30){
           for(i in 1..30)
               add(Quiz(title = "Quiz no: $i"))
        }
        adapter.quizList = dummyList
    }


}