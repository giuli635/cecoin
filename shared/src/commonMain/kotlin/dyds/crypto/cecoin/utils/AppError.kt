package dyds.crypto.cecoin.utils

import kotlinx.coroutines.CancellationException

sealed class AppError {
    abstract fun getMessage(): String

    data class NetworkError(val userMessage: String) : AppError() {
        override fun getMessage(): String =
            "$userMessage: Sin conexión a internet. Revisa tu Wi-Fi o datos móviles."
    }

    data class GenericError(val exception: Throwable, val userMessage: String) : AppError() {
        override fun getMessage(): String = buildString {
            append(userMessage)
            val detail = getUserFriendlyDetail(exception)
            if (detail != null) {
                append(": ")
                append(detail)
            }
        }

        private fun getUserFriendlyDetail(e: Throwable): String? = when {
            e is CancellationException -> null
            e.message != null -> e.message
            else -> "Error desconocido (${e.javaClass.simpleName})"
        }
    }
}
