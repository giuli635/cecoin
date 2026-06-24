package dyds.crypto.cecoin.core.domain.error

import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertEquals

private class HttpRequestTimeoutException(message: String) : Exception(message)

class AppErrorTest {
    @Test
    fun `NetworkError has error_network key and context as arg`() {
        val error = AppError.NetworkError("test_context")
        assertEquals("error_network", error.errorKey)
        assertEquals("test_context", error.args[0])
    }

    @Test
    fun `GenericError with HttpRequestTimeoutException has error_timeout key`() {
        val error = AppError.GenericError(HttpRequestTimeoutException("timeout"), "ctx")
        assertEquals("error_timeout", error.errorKey)
    }

    @Test
    fun `GenericError with CancellationException has error_cancelled key`() {
        val error = AppError.GenericError(CancellationException(), "ctx")
        assertEquals("error_cancelled", error.errorKey)
    }

    @Test
    fun `GenericError with exception message has error_with_message key`() {
        val error = AppError.GenericError(RuntimeException("msg"), "ctx")
        assertEquals("error_with_message", error.errorKey)
    }

    @Test
    fun `GenericError with no message has error_unknown key`() {
        val error = AppError.GenericError(RuntimeException(), "ctx")
        assertEquals("error_unknown", error.errorKey)
    }
}
