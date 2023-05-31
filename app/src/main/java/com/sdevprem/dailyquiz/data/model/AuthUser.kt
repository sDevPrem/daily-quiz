package com.sdevprem.dailyquiz.data.model

data class AuthUser(
    var uid: String? = null,
    val email: String,
    val password: String
)
