package com.sdevprem.dailyquiz.data.model

data class Quiz(
    var id : String = "",
    var title : String = "",
    var questions: MutableMap<String, Question> = mutableMapOf()
)
