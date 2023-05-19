package com.sdevprem.dailyquiz.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.sdevprem.dailyquiz.data.model.Quiz
import com.sdevprem.dailyquiz.data.util.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
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

    fun getQuiz(quizId: String) = callbackFlow<Response<Quiz>> {
        val response = firestore.collection("quizzes").document(quizId)
            .addSnapshotListener { value, error ->
                if (value == null || error != null) {
                    trySend(Response.Error(error))
                    return@addSnapshotListener
                }
                val quiz = value.toObject<Quiz>()
                if (quiz == null)
                    Response.Error(IOException("No quiz is found with the associated id"))
                else
                    trySend(Response.Success(quiz))
            }
        awaitClose {
            response.remove()
        }
    }.flowOn(Dispatchers.IO)

}