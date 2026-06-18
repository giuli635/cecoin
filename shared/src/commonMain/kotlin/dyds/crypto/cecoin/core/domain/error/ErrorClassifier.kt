package dyds.crypto.cecoin.core.domain.error

abstract class ErrorClassifier {
    protected abstract fun isNetworkError(exception: Throwable): Boolean

    fun classify(e: Throwable, userMessage: String): AppError {
        return if (isNetworkError(e)) {
            AppError.NetworkError(userMessage)
        } else {
            AppError.GenericError(e, userMessage)
        }
    }
}
