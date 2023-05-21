package com.sdevprem.dailyquiz.data.model

import android.icu.text.SimpleDateFormat
import android.os.Build
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.util.Locale

data class Quiz(
    @DocumentId
    var id: String = "",
    var questions: MutableMap<String, Question> = mutableMapOf(),
    var timestamp: Timestamp = Timestamp.now()
) {
    private val titleFormatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    } else {
        java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    }

    @get:Exclude
    val title: String
        get() = titleFormatter.format(timestamp.toDate())
}
