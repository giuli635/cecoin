package dyds.crypto.cecoin.core.domain.error

abstract class ErrorClassifier {
    protected abstract fun isNetworkError(exception: Throwable): Boolean

    fun classify(e: Throwable, context: String): AppError {
        return if (isNetworkError(e)) AppError.NetworkError(context)
        else AppError.GenericError(e, context)
    }
}
