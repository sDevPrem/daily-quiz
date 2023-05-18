package com.sdevprem.dailyquiz.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.sdevprem.dailyquiz.data.model.User
import com.sdevprem.dailyquiz.data.util.Response
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

}