package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.FakeTradePriceRepository
import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.domain.model.TradePrice
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetHistoricalPricesUseCaseTest {
    @Test
    fun `invoke delegates to repository with correct params`() = runTest {
        val expected = listOf(TradePrice("BTCUSDT", PricePoint(1000L, 50000.0)))
        val repo = FakeTradePriceRepository(historical = expected)
        val useCase = GetHistoricalPricesUseCaseImpl(repo)

        val result = useCase("BTCUSDT", "5m", 100)

        assertEquals(expected, result)
    }

    @Test
    fun `invoke uses default interval and limit`() = runTest {
        val expected = listOf(TradePrice("ETHUSDT", PricePoint(2000L, 3000.0)))
        val repo = FakeTradePriceRepository(historical = expected)
        val useCase = GetHistoricalPricesUseCaseImpl(repo)

        val result = useCase("ETHUSDT")

        assertEquals(expected, result)
    }
}
