package com.sdevprem.dailyquiz.data.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

sealed interface Response<out T> {
    object Idle : Response<Nothing>
    object Loading : Response<Nothing>
    data class Success<out T>(val data: T) : Response<T>
    data class Error(val e: Throwable?) : Response<Nothing>
}

fun <T> Flow<T>.toResponse(
    errorTransform: (Throwable) -> Throwable? = { it }
): Flow<Response<T>> = map<T, Response<T>> {
    Response.Success(it)
}.onStart {
    emit(Response.Loading)
}.catch {
    emit(Response.Error(errorTransform(it)))
}

fun <T, Y> Response<T>.exchangeResponse(
    errorTransform: (Throwable?) -> Throwable? = { it },
    successTransform: (T) -> Y,
): Response<Y> = when (this) {
    is Response.Loading -> this
    is Response.Idle -> this
    is Response.Error -> Response.Error(errorTransform(this.e))
    is Response.Success -> Response.Success(successTransform(this.data))
}