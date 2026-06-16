package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.FakeCryptoSymbolRepository
import dyds.crypto.cecoin.domain.model.CryptoSymbol
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAvailableSymbolsUseCaseTest {
    @Test
    fun `invoke returns symbols from repository`() = runTest {
        val expected = listOf(CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"))
        val repo = FakeCryptoSymbolRepository(expected)
        val useCase = GetAvailableSymbolsUseCase(repo)

        val result = useCase()

        assertEquals(expected, result)
    }

    @Test
    fun `invoke returns empty list when repository returns empty`() = runTest {
        val repo = FakeCryptoSymbolRepository(emptyList())
        val useCase = GetAvailableSymbolsUseCase(repo)

        val result = useCase()

        assertEquals(0, result.size)
    }
}
