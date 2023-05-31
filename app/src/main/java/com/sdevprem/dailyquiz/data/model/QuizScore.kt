package com.sdevprem.dailyquiz.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class QuizScore(
    @DocumentId
    var quizId: String = "",
    var score: Int = 0,
    var quizTime: Timestamp = Timestamp.now()
)
