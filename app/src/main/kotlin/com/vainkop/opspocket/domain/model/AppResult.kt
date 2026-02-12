package com.vainkop.opspocket.domain.model

sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val message: String, val code: Int? = null) : AppResult<Nothing>()
    data class NetworkError(val throwable: Throwable) : AppResult<Nothing>()
}
