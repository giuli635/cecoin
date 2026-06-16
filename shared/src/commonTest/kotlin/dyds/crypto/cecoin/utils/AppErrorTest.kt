package dyds.crypto.cecoin.utils

import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

class AppErrorTest {
    @Test
    fun `getMessage includes exception message when present`() {
        val error = AppError.GenericError(RuntimeException("connection failed"), "Error al cargar")
        assertEquals("Error al cargar: connection failed", error.getMessage())
    }

    @Test
    fun `getMessage uses simpleName when exception message is null`() {
        val error = AppError.GenericError(RuntimeException(null as String?), "Error al cargar")
        assertEquals("Error al cargar: Error desconocido (RuntimeException)", error.getMessage())
    }

    @Test
    fun `getMessage uses network error message for IOException`() {
        val error = AppError.GenericError(IOException("Network error"), "Error al cargar")
        assertEquals(
            "Error al cargar: Sin conexión a internet. Revisa tu Wi-Fi o datos móviles.",
            error.getMessage()
        )
    }
}
