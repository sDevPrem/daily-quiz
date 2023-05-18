package com.sdevprem.dailyquiz.data.model

data class Question(
    var description : String = "",
    var opt1 : String = "",
    var opt2 : String = "",
    var opt3 : String = "",
    var opt4 : String = "",
    var answer : String = "",
    var userAnswer : String = ""
)
