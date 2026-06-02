package dyds.crypto.cecoin.utils

sealed class AppError{
    abstract fun getMessage(): String

    data class GenericError(val exception: Throwable, val userMessage: String): AppError() {
        override fun getMessage(): String = userMessage + ": " + (exception.message ?: exception.javaClass.name)
    }
}
