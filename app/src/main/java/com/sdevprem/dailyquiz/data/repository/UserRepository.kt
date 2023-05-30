package com.sdevprem.dailyquiz.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.firestore.FirebaseFirestore
import com.sdevprem.dailyquiz.data.model.AuthUser
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
import java.io.IOException
import javax.inject.Inject

class UserRepository
@Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    @OptIn(DelicateCoroutinesApi::class)
    private val externalScope: CoroutineScope = GlobalScope
    private val ioDispatcher : CoroutineDispatcher = Dispatchers.IO

    suspend fun isUserSignIn() = withContext(ioDispatcher){
        firebaseAuth.currentUser != null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun signUp(authUser: AuthUser) = flow<Response<AuthUser>> {
        firebaseAuth.createUserWithEmailAndPassword(authUser.email, authUser.password)
            .apply {
                val result = suspendCancellableCoroutine { cont ->
                    addOnSuccessListener {
                        cont.resume(Response.Success(authUser.apply { uid = it.user!!.uid }), null)
                    }
                    addOnFailureListener {
                        cont.resume(Response.Error(it.toSignupException()), null)
                    }
                }
                if (result is Response.Success) {
                    if (createUserIfNotExist(result.data.uid!!)) emit(result)
                    else {
                        //if it failed to save the user in db
                        //then also delete the user
                        firebaseAuth.currentUser?.delete()
                        emit(Response.Error(IOException("Unable to create user")))
                    }
                } else emit(result)
            }
    }.flowOn(ioDispatcher)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun login(authUser: AuthUser) = flow<Response<AuthUser>> {
        firebaseAuth.signInWithEmailAndPassword(authUser.email, authUser.password)
            .apply {
                val result = suspendCancellableCoroutine { cont ->
                    addOnSuccessListener {
                        cont.resume(Response.Success(authUser), null)
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

}