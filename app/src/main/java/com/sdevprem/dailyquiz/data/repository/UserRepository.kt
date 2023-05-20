package com.sdevprem.dailyquiz.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.sdevprem.dailyquiz.data.model.User
import com.sdevprem.dailyquiz.data.util.Response
import com.sdevprem.dailyquiz.data.util.exception.toLoginException
import com.sdevprem.dailyquiz.data.util.exception.toSignupException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepository
@Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    @OptIn(DelicateCoroutinesApi::class)
    private val externalScope: CoroutineScope = GlobalScope
    private val ioDispatcher : CoroutineDispatcher = Dispatchers.IO

    suspend fun isUserSignIn() = withContext(ioDispatcher){
        firebaseAuth.currentUser != null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun signUp(user: User) = flow<Response<User>> {
        firebaseAuth.createUserWithEmailAndPassword(user.email,user.password)
            .apply{
                val result = suspendCancellableCoroutine { cont ->
                    addOnSuccessListener{
                        cont.resume(Response.Success(user),null)
                    }
                    addOnFailureListener {
                        cont.resume(Response.Error(it.toSignupException()),null)
                    }
                }
                emit(result)
            }
    }.flowOn(ioDispatcher)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun login(user : User) = flow<Response<User>> {
        firebaseAuth.signInWithEmailAndPassword(user.email,user.password)
            .apply {
                val result = suspendCancellableCoroutine { cont ->
                    addOnSuccessListener {
                        cont.resume(Response.Success(user), null)
                    }
                    addOnFailureListener {
                        cont.resume(Response.Error(it.toLoginException()), null)
                    }
                }
                emit(result)
            }
    }.flowOn(ioDispatcher)

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

}