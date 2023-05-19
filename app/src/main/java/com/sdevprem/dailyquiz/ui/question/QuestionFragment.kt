package com.sdevprem.dailyquiz.ui.question

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

@AndroidEntryPoint
class QuestionFragment : Fragment(R.layout.fragment_question) {
    lateinit var binding: FragmentQuestionBinding
    private val args by navArgs<QuestionFragmentArgs>()
    private val viewModel: QuestionVM by viewModels()
    private var adapter: QuestionOptAdapter? = null

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
        binding.apply {
            optList.layoutManager = LinearLayoutManager(requireContext())
            leftBtn.setOnClickListener {
                viewModel.decrementQuestion()
            }
            rightBtn.setOnClickListener {
                if (viewModel.isTheQuestionLast())
                    toast("Question submitted")
                else viewModel.incrementQuestion()
            }
        }
        viewModel.startFetchingQuiz(args.quizId)

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