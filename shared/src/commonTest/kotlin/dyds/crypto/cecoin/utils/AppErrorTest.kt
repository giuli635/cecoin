package dyds.crypto.cecoin.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class AppErrorTest {
    @Test
    fun `getMessage includes exception message when present`() {
        val error = AppError.GenericError(RuntimeException("connection failed"), "Failed to load")
        assertEquals("Failed to load: connection failed", error.getMessage())
    }

    @Test
    fun `getMessage uses class name when exception message is null`() {
        val error = AppError.GenericError(RuntimeException(null as String?), "Failed to load")
        assertEquals("Failed to load: java.lang.RuntimeException", error.getMessage())
    }
}
