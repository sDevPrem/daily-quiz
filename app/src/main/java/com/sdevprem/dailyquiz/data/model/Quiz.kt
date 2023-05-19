package com.sdevprem.dailyquiz.data.model

import com.google.firebase.firestore.DocumentId

data class Quiz(
    @DocumentId
    var id: String = "",
    var title: String = "",
    var questions: MutableMap<String, Question> = mutableMapOf()
)
