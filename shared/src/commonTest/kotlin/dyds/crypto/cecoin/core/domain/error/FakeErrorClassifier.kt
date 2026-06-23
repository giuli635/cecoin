package dyds.crypto.cecoin.core.domain.error

fun fakeErrorClassifier(isNetworkError: Boolean = false): ErrorClassifier = object : ErrorClassifier() {
    override fun isNetworkError(exception: Throwable) = isNetworkError
}
