package com.sdevprem.dailyquiz.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sdevprem.dailyquiz.data.model.Quiz
import com.sdevprem.dailyquiz.databinding.QuizListItemBinding
import com.sdevprem.dailyquiz.uitls.ColorPicker
import com.sdevprem.dailyquiz.uitls.IconPicker

class QuizAdapter(
    quizList : List<Quiz>
) : Adapter<QuizAdapter.QuizVH>() {

    var quizList : List<Quiz> = quizList
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class QuizVH(val binding : QuizListItemBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizVH {
        return QuizVH(
            QuizListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return quizList.size
    }

    override fun onBindViewHolder(holder: QuizVH, position: Int) {
        holder.binding.apply {
            quizTitle.text = quizList[position].title
            cardContainer.setCardBackgroundColor(Color.parseColor(ColorPicker.getColor()))
            quizImg.setImageResource(IconPicker.getIcon())
        }
    }
}