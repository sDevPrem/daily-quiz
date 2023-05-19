package com.sdevprem.dailyquiz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sdevprem.dailyquiz.R
import com.sdevprem.dailyquiz.data.model.Question
import com.sdevprem.dailyquiz.databinding.FragmentQuestionBinding

class QuestionFragment : Fragment(R.layout.fragment_question) {
    lateinit var binding: FragmentQuestionBinding

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
            this.question.text = question.description
            optList.layoutManager = LinearLayoutManager(requireContext())
            optList.adapter = QuestionOptAdapter(question)
        }
    }
}