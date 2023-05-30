package com.sdevprem.dailyquiz.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    var uid: String? = null,
    var attemptedQuizzes: Map<String/*quiz_id*/, QuizScore> = emptyMap()
)
