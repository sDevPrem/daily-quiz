package com.sdevprem.dailyquiz.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.sdevprem.dailyquiz.data.model.Quiz
import com.sdevprem.dailyquiz.data.model.QuizScore
import com.sdevprem.dailyquiz.data.model.User
import com.sdevprem.dailyquiz.data.util.Response
import com.sdevprem.dailyquiz.data.util.filter.QuizFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import javax.inject.Inject

class QuizRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {

    fun getQuizList(quizFilter: QuizFilter) = callbackFlow<Response<List<Quiz>>> {
        val response = firestore.collection("quizzes")
            .whereLessThanOrEqualTo("timestamp", quizFilter.toDate)
            .whereGreaterThanOrEqualTo("timestamp", quizFilter.fromDate)
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

    fun saveUserScore(score: QuizScore, quizId: String) {
        firestore.collection("users")
            .document(firebaseAuth.currentUser?.uid ?: return)
            .update("attemptedQuizzes.$quizId", score)
    }

    fun getUserScore(quizId: String) = callbackFlow<Response<QuizScore?>> {
        if (firebaseAuth.currentUser == null)
            return@callbackFlow
        firestore.collection("users")
            .document(firebaseAuth.currentUser!!.uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result.toObject<User>()?.attemptedQuizzes?.toList()?.let { list ->
                        list.forEach {
                            if (it.first == quizId) {
                                trySend(Response.Success(it.second))
                                return@let
                            }
                        }
                        trySend(Response.Success(null))
                    }
                } else trySend(Response.Error(task.exception?.cause))
            }
        awaitClose {}
    }.flowOn(Dispatchers.IO)

}