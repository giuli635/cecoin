package dyds.crypto.cecoin.domain.chart.usecase

import dyds.crypto.cecoin.domain.chart.FakeTradePriceRepository
import dyds.crypto.cecoin.domain.chart.model.PricePoint
import dyds.crypto.cecoin.domain.chart.model.TradePrice
import dyds.crypto.cecoin.utils.error.ErrorClassifier
import dyds.crypto.cecoin.utils.state.Fallible
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetHistoricalPricesUseCaseTest {
    private val classifier = object : ErrorClassifier() {
        override fun isNetworkError(e: Throwable) = false
    }

    @Test
    fun `invoke delegates to repository with correct params`() = runTest {
        val expected = listOf(TradePrice("BTCUSDT", PricePoint(1000L, 50000.0)))
        val repo = FakeTradePriceRepository(historical = expected)
        val useCase = GetHistoricalPricesUseCaseImpl(repo, classifier)

        val result = useCase("BTCUSDT", "5m", 100)

        val success = result as Fallible.Success
        assertEquals(expected, success.value)
    }

    @Test
    fun `invoke uses default interval and limit`() = runTest {
        val expected = listOf(TradePrice("ETHUSDT", PricePoint(2000L, 3000.0)))
        val repo = FakeTradePriceRepository(historical = expected)
        val useCase = GetHistoricalPricesUseCaseImpl(repo, classifier)

        val result = useCase("ETHUSDT")

        val success = result as Fallible.Success
        assertEquals(expected, success.value)
    }
}
