package com.sdevprem.dailyquiz.ui.question

import android.os.Build
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
import com.sdevprem.dailyquiz.data.model.Question
import com.sdevprem.dailyquiz.data.util.Response
import com.sdevprem.dailyquiz.databinding.FragmentQuestionBinding
import com.sdevprem.dailyquiz.uitls.launchInLifecycle
import com.sdevprem.dailyquiz.uitls.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuestionFragment : Fragment(R.layout.fragment_question) {
    lateinit var binding: FragmentQuestionBinding
    private val args by navArgs<QuestionFragmentArgs>()
    private val viewModel: QuestionVM by hiltNavGraphViewModels(R.id.question_nav)
    private var adapter: QuestionOptAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.quizId = args.quizId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentQuestionBinding.inflate(
            inflater,
            container,
            false
        ).apply {
            binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val question = Question(
            description = "A stranger walks into your house. What do you do/say?",
            opt1 = "Who dares enter unicorn land?",
            opt2 = "Call 911",
            opt3 = "Fly to safety.",
            opt4 = "706-888-999",
        )
        if (!args.isRetry)
            lifecycleScope.launch {
                viewModel.userScore.collectLatest {
                    when (it) {
                        is Response.Success -> {
                            if (it.data == null) {
                                fetchQuiz()
                            } else {
                                findNavController().navigate(
                                    QuestionFragmentDirections
                                        .actionQuestionFragmentToQuizResultFragment(score = it.data.score)
                                )
                            }
                        }

                        is Response.Loading -> binding.apply {
                            leftBtn.visibility = View.INVISIBLE
                            rightBtn.isEnabled = false
                            optList.isVisible = false
                            progressBar.isVisible = true
                        }

                        is Response.Error -> toast(it.e?.message ?: "")
                        else -> {}
                    }
                }
            }
        else fetchQuiz()

    }

    private fun fetchQuiz() {
        binding.apply {
            optList.layoutManager = LinearLayoutManager(requireContext())
            leftBtn.setOnClickListener {
                viewModel.decrementQuestion()
            }
            rightBtn.setOnClickListener {
                if (viewModel.isTheQuestionLast()) {
                    viewModel.saveUserScore()
                    findNavController().navigate(
                        QuestionFragmentDirections
                            .actionQuestionFragmentToQuizResultFragment()
                    )
                } else viewModel.incrementQuestion()
            }
        }
        launchInLifecycle {
            viewModel.currentQuestion.collectLatest {
                when (it) {
                    is Response.Success -> bindViews(it.data)
                    is Response.Loading -> binding.apply {
                        leftBtn.visibility = View.INVISIBLE
                        rightBtn.isEnabled = false
                        optList.isVisible = false
                        progressBar.isVisible = true
                    }

                    is Response.Error -> toast(it.e?.message ?: "")
                    else -> {}
                }
            }
        }
    }

    private fun bindViews(question: Question) = binding.apply {
        progressBar.isVisible = false
        optList.isVisible = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            bottomQuestionProgress.setProgress(viewModel.getCompletionPercentage(), true)
        } else bottomQuestionProgress.progress = viewModel.getCompletionPercentage()

        setQuestionToAdapter(question)
        this.question.text = question.description
        setUpButtons()
    }


    private fun setUpButtons() = binding.apply {
        rightBtn.isEnabled = true
        leftBtn.visibility = if (viewModel.isTheQuestionFirst()) View.INVISIBLE else View.VISIBLE
        rightBtn.text = if (viewModel.isTheQuestionLast()) "Submit" else "Next"
    }

    private fun setQuestionToAdapter(question: Question) = adapter?.let {
        it.question = question
    } ?: run {
        adapter = QuestionOptAdapter(question)
        binding.optList.adapter = adapter
    }
}