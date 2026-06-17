package dyds.crypto.cecoin.core.utils.error

import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals

class AppErrorTest {
    @Test
    fun `NetworkError getMessage returns formatted message`() {
        val error = AppError.NetworkError("Error de red")
        assertEquals("Error de red: Sin conexión a internet. Revisa tu Wi-Fi o datos móviles.", error.getMessage())
    }

    @Test
    fun `GenericError with CancellationException returns userMessage only`() {
        val error = AppError.GenericError(CancellationException("cancel"), "Error")
        assertEquals("Error", error.getMessage())
    }

    @Test
    fun `GenericError with exception message returns userMessage and detail`() {
        val error = AppError.GenericError(RuntimeException("algo salió mal"), "Error")
        assertEquals("Error: algo salió mal", error.getMessage())
    }

    @Test
    fun `GenericError with null message returns unknown error`() {
        val error = AppError.GenericError(RuntimeException(), "Error")
        val message = error.getMessage()
        assertEquals("Error: Error desconocido (RuntimeException)", message)
    }
}
