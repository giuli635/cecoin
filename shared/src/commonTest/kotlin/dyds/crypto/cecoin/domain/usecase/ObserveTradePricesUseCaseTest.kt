package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.FakeTradePriceRepository
import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.domain.model.TradePrice
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveTradePricesUseCaseTest {
    @Test
    fun `invoke returns flow from repository`() = runTest {
        val expected = TradePrice("BTCUSDT", PricePoint(1000L, 50000.0))
        val repo = FakeTradePriceRepository(tradeFlow = flowOf(expected))
        val useCase = ObserveTradePricesUseCase(repo)

        val result = useCase("BTCUSDT")

        assertEquals(expected, result.first())
    }
}
