package com.sdevprem.dailyquiz.data.model

data class Question(
    var description: String = "",
    var opt1: String = "",
    var opt2: String = "",
    var opt3: String = "",
    var opt4: String = "",
    var answer: Int = -1, //will be given by network
    var userAnswer: Int = -1 //no option selected
)
