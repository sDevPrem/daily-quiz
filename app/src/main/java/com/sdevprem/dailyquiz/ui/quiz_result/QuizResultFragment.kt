package com.sdevprem.dailyquiz.ui.quiz_result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
    private val args by navArgs<QuizResultFragmentArgs>()

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
        binding.homeBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.retryBtn.setOnClickListener {
            viewModel.reset()
            findNavController().navigate(
                QuizResultFragmentDirections
                    .actionQuizResultFragmentToQuestionFragment(
                        viewModel.quizId ?: return@setOnClickListener
                    )
            )
        }
        if (args.score == -1)
            lifecycleScope.launch {
                showResult()
            }
        else binding.apply {
            userScore.text = getString(R.string.user_quiz_result_score, args.score)
            userAnswersLabel.isVisible = false
        }
    }

    private suspend fun showResult() = withContext(Dispatchers.Default) {
        var totalScore = 0
        viewModel.questionList.forEach {
            totalScore += if (it.userAnswer == it.answer) 10 else 0
        }
        withContext(Dispatchers.Main) {
            binding.apply {
                userAnswerList.layoutManager = LinearLayoutManager(requireContext())
                userAnswerList.adapter = QuizResultAdapter(viewModel.questionList)

            }
            binding.userScore.text = getString(R.string.user_quiz_result_score, totalScore)
        }
    }
}