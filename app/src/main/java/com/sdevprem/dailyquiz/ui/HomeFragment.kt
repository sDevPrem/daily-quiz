package com.sdevprem.dailyquiz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.sdevprem.dailyquiz.R
import com.sdevprem.dailyquiz.data.model.Quiz
import com.sdevprem.dailyquiz.data.repository.QuizRepository
import com.sdevprem.dailyquiz.data.util.Response
import com.sdevprem.dailyquiz.databinding.FragmentHomeBinding
import com.sdevprem.dailyquiz.uitls.launchInLifecycle
import com.sdevprem.dailyquiz.uitls.toast
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(){
    lateinit var binding: FragmentHomeBinding
    private val adapter = QuizAdapter(emptyList()) {
        findNavController().navigate(
            HomeFragmentDirections
                .actionHomeFragmentToQuestionFragment(quizList[it].id)
        )
    }
    private val viewModel: HomeVM by viewModels()

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
        binding.toolbar.setupWithNavController(findNavController(), configuration)

        binding.quizGrid.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.quizGrid.adapter = adapter

        launchInLifecycle {
            viewModel.quiz.collectLatest {
                when (it) {
                    is Response.Success -> binding.apply {
                        progressBar.isVisible = false
                        quizGrid.isVisible = true
                        adapter.quizList = it.data
                    }

                    is Response.Loading -> binding.apply {
                        quizGrid.isVisible = false
                        progressBar.isVisible = true
                    }

                    is Response.Error -> toast("Something went wrong. Please try again later")
                    else -> {}
                }
            }
        }
    }

    fun getList() = adapter.quizList

    private fun populateDummyData() {
        val dummyList = buildList<Quiz>(30) {
            for (i in 1..30)
                add(Quiz(title = "Quiz no: $i"))
        }
        adapter.quizList = dummyList
    }
}

@HiltViewModel
class HomeVM @Inject constructor(
    quizRepository: QuizRepository
) : ViewModel() {
    val quiz = quizRepository.getQuizList()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(10_000),
            Response.Loading
        )
}