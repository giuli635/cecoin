package dyds.crypto.cecoin.core.domain.error

sealed class AppError {
    abstract val errorKey: String
    abstract val args: Array<out Any>

    data class NetworkError(val context: String) : AppError() {
        override val errorKey = "error_network"
        override val args = arrayOf(context)
    }

    data class GenericError(
        val exception: Throwable,
        val context: String,
    ) : AppError() {
        override val errorKey get() = when {
            exception is kotlinx.coroutines.CancellationException -> "error_cancelled"
            exception.message != null -> "error_with_message"
            else -> "error_unknown"
        }
        override val args get() = when (errorKey) {
            "error_cancelled" -> arrayOf(context)
            "error_with_message" -> arrayOf(context, exception.message!!)
            else -> arrayOf(context, exception.javaClass.simpleName)
        }
    }
}
