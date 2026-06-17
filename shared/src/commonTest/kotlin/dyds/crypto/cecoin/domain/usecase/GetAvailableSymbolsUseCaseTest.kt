package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.FakeCryptoSymbolRepository
import dyds.crypto.cecoin.domain.model.CryptoSymbol
import dyds.crypto.cecoin.utils.ErrorClassifier
import dyds.crypto.cecoin.utils.Fallible
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetAvailableSymbolsUseCaseTest {
    private val classifier = object : ErrorClassifier() {
        override fun isNetworkError(e: Throwable) = false
    }

    @Test
    fun `invoke returns symbols from repository`() = runTest {
        val expected = listOf(CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"))
        val repo = FakeCryptoSymbolRepository(expected)
        val useCase = GetAvailableSymbolsUseCaseImpl(repo, classifier)

        val result = useCase()

        val success = result as Fallible.Success
        assertEquals(expected, success.value)
    }

    @Test
    fun `invoke returns empty list when repository returns empty`() = runTest {
        val repo = FakeCryptoSymbolRepository(emptyList())
        val useCase = GetAvailableSymbolsUseCaseImpl(repo, classifier)

        val result = useCase()

        val success = result as Fallible.Success
        assertEquals(0, success.value.size)
    }

    @Test
    fun `invoke returns Failed when repository throws`() = runTest {
        val repo = FakeCryptoSymbolRepository(exception = RuntimeException("repo fail"))
        val useCase = GetAvailableSymbolsUseCaseImpl(repo, classifier)

        val result = useCase()

        assertIs<Fallible.Failed>(result)
    }
}
