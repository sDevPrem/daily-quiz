package com.sdevprem.dailyquiz.data.util.filter

import com.sdevprem.dailyquiz.uitls.DateUtils.setMaximumTimeOfMth
import com.sdevprem.dailyquiz.uitls.DateUtils.setMinimumTimeOfMth
import java.util.Calendar
import java.util.Date

data class QuizFilter(
    var fromDate: Date = Calendar.getInstance().setMinimumTimeOfMth().time,
    var toDate: Date = Calendar.getInstance().setMaximumTimeOfMth().time
)
