package com.sdevprem.dailyquiz.data.model

import com.google.firebase.firestore.DocumentId

//for future use to store some more info about the user
data class User(
    @DocumentId
    var uid: String? = null,
)
