package dyds.crypto.cecoin.core.domain.error

import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertEquals

class AppErrorTest {
    @Test
    fun `NetworkError holds Dynamic UiText`() {
        val error = AppError.NetworkError(UiText.Dynamic("msg"))
        assertIs<UiText.Dynamic>(error.uiText)
        assertEquals("msg", (error.uiText as UiText.Dynamic).value)
    }

    @Test
    fun `GenericError with CancellationException holds Dynamic`() {
        val error = AppError.GenericError(
            CancellationException("cancel"),
            UiText.Dynamic("Error"),
        )
        assertIs<UiText.Dynamic>(error.uiText)
    }

    @Test
    fun `GenericError with exception message holds Dynamic`() {
        val error = AppError.GenericError(
            RuntimeException("algo salió mal"),
            UiText.Dynamic("Error: algo salió mal"),
        )
        assertEquals("Error: algo salió mal", (error.uiText as UiText.Dynamic).value)
    }
}
