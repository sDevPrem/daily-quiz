package com.sdevprem.dailyquiz.data.util

sealed interface Response<out T>{
    object Idle : Response<Nothing>
    object Loading : Response<Nothing>
    data class Success<out T>(val data : T) : Response<T>
    data class Error(val e: Throwable?) : Response<Nothing>
}