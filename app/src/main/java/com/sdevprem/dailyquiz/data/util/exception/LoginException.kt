package com.sdevprem.dailyquiz.data.util.exception

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import java.io.IOException

sealed class LoginException(message : String) : IOException(message){
    object InvalidCredentialException : LoginException("The given email or password is invalid.")
    object EmailNotVerifiedException :
        LoginException("Email is not verified. Verify email and try again.")
}

fun Exception.toLoginException() = when(this){
    is FirebaseAuthInvalidCredentialsException -> LoginException.InvalidCredentialException
    is FirebaseAuthInvalidUserException -> LoginException.InvalidCredentialException
    else -> this
}