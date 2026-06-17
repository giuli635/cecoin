package dyds.crypto.cecoin.utils.error

fun fakeErrorClassifier(isNetworkError: Boolean = false): ErrorClassifier = object : ErrorClassifier() {
    override fun isNetworkError(e: Throwable) = isNetworkError
}
