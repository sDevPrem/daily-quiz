package com.sdevprem.dailyquiz.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sdevprem.dailyquiz.data.model.Quiz
import com.sdevprem.dailyquiz.data.util.Response
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class QuizRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    fun getQuizList() = callbackFlow<Response<List<Quiz>>> {
        val response = firestore.collection("quizzes")
            .addSnapshotListener { value, error ->
                if (value == null || error != null) {
                    trySend(Response.Error(error))
                    return@addSnapshotListener
                }
                val list = value.toObjects(Quiz::class.java)
                trySend(Response.Success(list))
            }

        awaitClose {
            response.remove()
        }
    }

}