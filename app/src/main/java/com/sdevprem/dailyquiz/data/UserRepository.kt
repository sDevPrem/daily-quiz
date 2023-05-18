package com.sdevprem.dailyquiz.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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

}