package com.sdevprem.dailyquiz.ui.quiz_result

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sdevprem.dailyquiz.R
import com.sdevprem.dailyquiz.data.model.Question
import com.sdevprem.dailyquiz.databinding.QuizResultAnswerListItemBinding

class QuizResultAdapter(
    private val list: List<Question>
) : Adapter<QuizResultAdapter.UserAnswerVM>() {
    inner class UserAnswerVM(val binding: QuizResultAnswerListItemBinding) :
        ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAnswerVM {
        return UserAnswerVM(
            QuizResultAnswerListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: UserAnswerVM, position: Int): Unit =
        with(holder.binding) {
            val currentQuestion = list[position]
            question.text = root.context.getString(
                R.string.quiz_result_question,
                position + 1,
                currentQuestion.description
            )
            if (currentQuestion.userAnswer < 1) {
                answerFeedbackImg.isVisible = false
                userAnswer.text = ""
                userAnswerLabel.setTextColor(root.context.resources.getColor(android.R.color.black))
            } else if (currentQuestion.answer == currentQuestion.userAnswer) {
                answerFeedbackImg.isVisible = true
                answerFeedbackImg.setImageResource(R.drawable.baseline_check_24)
                answerFeedbackImg.imageTintList = ColorStateList.valueOf(Color.GREEN)
                userAnswerLabel.setTextColor(Color.GREEN)
            } else {
                answerFeedbackImg.isVisible = true
                answerFeedbackImg.setImageResource(R.drawable.baseline_close_24)
                answerFeedbackImg.imageTintList = ColorStateList.valueOf(Color.RED)
                userAnswerLabel.setTextColor(Color.RED)
            }
            userAnswer.text = currentQuestion.getOption(currentQuestion.userAnswer)
        }
}