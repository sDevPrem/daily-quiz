package com.sdevprem.dailyquiz.data.model

import com.google.firebase.Timestamp

data class QuizScore(
    var score: Int = 0,
    var timestamp: Timestamp = Timestamp.now()
)
