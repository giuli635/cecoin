package dyds.crypto.cecoin.utils

import kotlin.test.Test
import kotlin.test.assertIs

class ErrorClassifierTest {
    @Test
    fun `classify with network error returns NetworkError`() {
        val classifier = object : ErrorClassifier() {
            override fun isNetworkError(e: Throwable) = true
        }
        val result = classifier.classify(RuntimeException("timeout"), "message")
        assertIs<AppError.NetworkError>(result)
    }

    @Test
    fun `classify with non-network error returns GenericError`() {
        val classifier = object : ErrorClassifier() {
            override fun isNetworkError(e: Throwable) = false
        }
        val result = classifier.classify(RuntimeException("fail"), "message")
        assertIs<AppError.GenericError>(result)
    }
}
