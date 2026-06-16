package dyds.crypto.cecoin.utils

import java.io.IOException

sealed class AppError {
    abstract fun getMessage(): String

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
            e.isNetworkError() -> "Sin conexión a internet. Revisa tu Wi-Fi o datos móviles."
            e.message != null -> e.message
            else -> "Error desconocido (${e.javaClass.simpleName})"
        }

        private fun Throwable.isNetworkError(): Boolean =
            this is IOException ||
                javaClass.name.contains("UnresolvedAddressException") ||
                javaClass.name.contains("NoRouteToHostException") ||
                message?.contains("No route to host") == true ||
                message?.contains("Network is unreachable") == true
    }
}
