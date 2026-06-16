package dyds.crypto.cecoin.utils

abstract class ErrorClassifier {
    protected abstract fun isNetworkError(exception: Throwable): Boolean

    fun classify(exception: Throwable, userMessage: String): AppError {
        return if (isNetworkError(exception)) {
            AppError.NetworkError(userMessage)
        } else {
            AppError.GenericError(exception, userMessage)
        }
    }
}
