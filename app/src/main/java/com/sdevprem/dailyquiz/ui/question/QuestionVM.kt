package com.sdevprem.dailyquiz.ui.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sdevprem.dailyquiz.data.model.Question
import com.sdevprem.dailyquiz.data.model.Quiz
import com.sdevprem.dailyquiz.data.model.QuizScore
import com.sdevprem.dailyquiz.data.repository.QuizRepository
import com.sdevprem.dailyquiz.data.util.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionVM @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {
    var questionList = emptyList<Question>()
        private set
    private val _currentQuestion = MutableStateFlow<Response<Question>>(Response.Loading)
    val currentQuestion: StateFlow<Response<Question>> = _currentQuestion
    private var currentQuestionIndex = 0
    private var quizId: String? = null
    private var quiz: Quiz? = null

    fun startFetchingQuiz(id: String) {
        if (quizId != null)
            return
        quizId = id
        quizRepository.getQuiz(id)
            .onStart { emit(Response.Loading) }
            .onEach {
                when (it) {
                    is Response.Success -> {
                        quiz = it.data
                        questionList = it.data.questions.values.toList()
                        currentQuestionIndex = 0
                        _currentQuestion.value = Response.Success(questionList[0])
                    }
                    //pass the error directly to the UI
                    is Response.Error -> _currentQuestion.value = it
                    is Response.Loading -> _currentQuestion.value = it
                    else -> {}
                }
            }.launchIn(viewModelScope)
    }

    fun incrementQuestion() {
        if (!isTheQuestionLast()) {
            _currentQuestion.value = Response.Success(questionList[++currentQuestionIndex])
        }
    }

    fun decrementQuestion() {
        if (!isTheQuestionFirst()) {
            _currentQuestion.value = Response.Success(questionList[--currentQuestionIndex])
        }
    }

    fun isTheQuestionFirst() = currentQuestionIndex == 0
    fun isTheQuestionLast() = currentQuestionIndex == questionList.size - 1

    fun getCompletionPercentage() = if (questionList.isNotEmpty())
        ((currentQuestionIndex + 1) / questionList.size.toFloat() * 100f).toInt()
    else 0

    fun saveUserScore() = viewModelScope.launch(Dispatchers.IO) {
        var totalScore = 0
        questionList.forEach {
            totalScore += if (it.userAnswer == it.answer) 10 else 0
        }
        quizRepository.saveUserScore(
            QuizScore(totalScore, quiz?.timestamp ?: return@launch),
            quizId ?: return@launch
        )
    }
}