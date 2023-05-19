package com.sdevprem.dailyquiz.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sdevprem.dailyquiz.R
import com.sdevprem.dailyquiz.data.model.Question
import com.sdevprem.dailyquiz.databinding.QuestionOptListItemBinding

class QuestionOptAdapter(
    question: Question
) : Adapter<QuestionOptAdapter.OptionVH>() {

    var question: Question = question
        set(value) {
            field = value
            options = listOf(question.opt1, question.opt2, question.opt3, question.opt4)
            notifyDataSetChanged()
        }
    private var options: List<String> =
        listOf(question.opt1, question.opt2, question.opt3, question.opt4)

    inner class OptionVH(val binding: QuestionOptListItemBinding) : ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (question.userAnswer != adapterPosition) {
                    if (question.userAnswer != -1)
                        notifyItemChanged(question.userAnswer)
                    question.userAnswer = adapterPosition
                    it.setBackgroundResource(R.drawable.selected_opt_item_bg)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionVH {
        return OptionVH(
            QuestionOptListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = 4

    override fun onBindViewHolder(holder: OptionVH, position: Int) {
        holder.binding.option.text = options[position]
        if (position == question.userAnswer) {
            holder.binding.root.setBackgroundResource(R.drawable.selected_opt_item_bg)
        } else holder.binding.root.background = null
    }
}