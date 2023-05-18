package com.sdevprem.dailyquiz.data.util.exception

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import java.io.IOException

sealed class SignupException(message : String) : IOException(message){
    object EmailAlreadyInUseException : SignupException("The email address is already in use by another account")
    object WeakPasswordException : SignupException("The given password is weak. Use string password.")
    object InvalidCredentialException : SignupException("Invalid credentials")
}

fun Exception.toSignupException() = when(this) {
        is FirebaseAuthUserCollisionException -> SignupException.EmailAlreadyInUseException
        is FirebaseAuthWeakPasswordException -> SignupException.WeakPasswordException
        is FirebaseAuthInvalidCredentialsException -> SignupException.InvalidCredentialException
        else -> IOException(message)
}

