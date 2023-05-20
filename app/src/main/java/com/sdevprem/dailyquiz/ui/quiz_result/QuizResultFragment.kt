package com.sdevprem.dailyquiz.ui.quiz_result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sdevprem.dailyquiz.R
import com.sdevprem.dailyquiz.databinding.FragmentQuizResultBinding
import com.sdevprem.dailyquiz.ui.question.QuestionVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuizResultFragment : Fragment() {
    lateinit var binding: FragmentQuizResultBinding
    private val viewModel: QuestionVM by hiltNavGraphViewModels(R.id.question_nav)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentQuizResultBinding.inflate(
            inflater,
            container,
            false
        ).apply {
            binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            userAnswerList.layoutManager = LinearLayoutManager(requireContext())
            userAnswerList.adapter = QuizResultAdapter(viewModel.questionList)
            homeBtn.setOnClickListener {
                findNavController().navigateUp()
            }
        }
        lifecycleScope.launch {
            showResult()
        }
    }

    private suspend fun showResult() = withContext(Dispatchers.Default) {
        var totalScore = 0
        viewModel.questionList.forEach {
            totalScore += if (it.userAnswer == it.answer) 10 else 0
        }
        withContext(Dispatchers.Main) {
            binding.userScore.text = getString(R.string.user_quiz_result_score, totalScore)
        }
    }
}