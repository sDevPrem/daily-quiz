package com.sdevprem.dailyquiz.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.sdevprem.dailyquiz.data.model.AuthUser
import com.sdevprem.dailyquiz.data.model.QuizScore
import com.sdevprem.dailyquiz.data.model.User
import com.sdevprem.dailyquiz.data.util.Response
import com.sdevprem.dailyquiz.data.util.exception.LoginException
import com.sdevprem.dailyquiz.data.util.exception.toLoginException
import com.sdevprem.dailyquiz.data.util.exception.toSignupException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class UserRepository
@Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private val ioDispatcher : CoroutineDispatcher = Dispatchers.IO

    suspend fun isUserSignIn() = withContext(ioDispatcher){
        firebaseAuth.currentUser?.isEmailVerified == true
    }

    fun signUp(authUser: AuthUser) = flow<Response<AuthUser>> {
        val result =
            firebaseAuth.createUserWithEmailAndPassword(authUser.email, authUser.password).await()
        sendVerificationEmail()
        emit(Response.Success(authUser.copy(uid = result.user!!.uid)))
    }.catch {
        emit(
            Response.Error(
                if (it is Exception)
                    it.toSignupException()
                else it
            )
        )
    }.flowOn(ioDispatcher)
        .onStart { emit(Response.Loading) }

    private suspend fun sendVerificationEmail() {
        firebaseAuth.currentUser?.sendEmailVerification()?.await()
    }

    fun login(authUser: AuthUser) = flow<Response<AuthUser>> {
        firebaseAuth.signInWithEmailAndPassword(authUser.email, authUser.password)
            .await().user?.let {
                if (!it.isEmailVerified) {
                    it.sendEmailVerification()
                    throw LoginException.EmailNotVerifiedException
                } else {
                    val isUserCreated = createUserIfNotExist(it.uid)
                    if (isUserCreated)
                        emit(Response.Success(authUser.copy(uid = it.uid)))
                    else {
                        firebaseAuth.signOut()
                        throw IOException("Unable to create user")
                    }
                }
            }
    }.catch {
        emit(
            Response.Error(
                if (it is Exception && it !is LoginException)
                    it.toLoginException()
                else it
            )
        )

    }.flowOn(ioDispatcher)
        .onStart { emit(Response.Loading) }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun logout() = flow {
        firebaseAuth.signOut()
        val result = suspendCancellableCoroutine {
            val listener = object : AuthStateListener {
                var flag = true
                override fun onAuthStateChanged(p0: FirebaseAuth) {
                    firebaseAuth.removeAuthStateListener(this)
                    if (flag)
                        if (p0.currentUser == null)
                            it.resume(true, null)
                        else it.resume(false, null)
                    flag = false
                }
            }
            firebaseAuth.addAuthStateListener(listener)
        }
        emit(result)
    }.flowOn(ioDispatcher)

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun createUserIfNotExist(uid: String) = suspendCancellableCoroutine<Boolean> { cont ->
        val uidRef = firestore.collection("users").document(uid)
        uidRef.get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    if (!it.result.exists()) {
                        uidRef.set(
                            User()
                        ).addOnSuccessListener { cont.resume(true, null) }
                            .addOnFailureListener { cont.resume(false, null) }
                    } else cont.resume(true, null)

                } else cont.resume(false, null)
            }
    }

    fun getUserQuizScore() = callbackFlow<Response<List<QuizScore>>> {
        var listener: ListenerRegistration? = null
        firebaseAuth.currentUser?.uid?.let {
            listener = firestore.collection("users").document(it)
                .collection("attemptedQuizzes")
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        trySend(Response.Error(error))
                        return@addSnapshotListener
                    } else if (value == null) {
                        trySend(Response.Error(IOException("User not found")))
                        return@addSnapshotListener
                    }
                    trySend(Response.Success(value.toObjects<QuizScore>()))
                }
        } ?: trySend(Response.Error(IOException("User not found")))

        awaitClose {
            listener?.remove()
        }
    }

    fun saveUserScore(score: QuizScore, quizId: String) {
        firestore.collection("users")
            .document(firebaseAuth.currentUser?.uid ?: return)
            .collection("attemptedQuizzes")
            .document(quizId)
            .set(score)
    }

    fun getUserScore(quizId: String) = callbackFlow<Response<QuizScore?>> {
        if (firebaseAuth.currentUser == null)
            return@callbackFlow
        firestore.collection("users")
            .document(firebaseAuth.currentUser!!.uid)
            .collection("attemptedQuizzes")
            .document(quizId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Response.Success(task.result.toObject<QuizScore>()))
                } else trySend(Response.Error(task.exception?.cause))
            }
        awaitClose {}
    }.flowOn(Dispatchers.IO)

}