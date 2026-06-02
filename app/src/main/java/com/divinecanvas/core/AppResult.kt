package com.divinecanvas.core

/**
 * Lightweight result type for data operations. [Success] may carry a [fromCache] flag so the UI can
 * surface a gentle "showing offline copy" note without treating it as an error.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T, val fromCache: Boolean = false) : AppResult<T>

    data class Failure(val message: String, val cause: Throwable? = null) : AppResult<Nothing>

    fun getOrNull(): T? = (this as? Success)?.data

    fun <R> map(transform: (T) -> R): AppResult<R> =
        when (this) {
            is Success -> Success(transform(data), fromCache)
            is Failure -> this
        }
}

inline fun <T> runCatchingResult(block: () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (e: Exception) {
        AppResult.Failure(e.message ?: "Unexpected error", e)
    }
