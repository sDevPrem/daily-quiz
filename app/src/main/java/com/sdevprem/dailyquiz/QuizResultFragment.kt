package com.sdevprem.dailyquiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.sdevprem.dailyquiz.databinding.FragmentQuizResultBinding
import com.sdevprem.dailyquiz.ui.question.QuestionVM
import com.sdevprem.dailyquiz.uitls.toast

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
        assert(viewModel.isTheQuestionLast()) //check if the viewModel is shared
        toast(viewModel.isTheQuestionLast().toString())
    }
}