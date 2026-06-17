package dyds.crypto.cecoin.domain.chart.usecase

import dyds.crypto.cecoin.domain.chart.FakeTradePriceRepository
import dyds.crypto.cecoin.domain.chart.model.PricePoint
import dyds.crypto.cecoin.domain.chart.model.TradePrice
import dyds.crypto.cecoin.utils.error.ErrorClassifier
import dyds.crypto.cecoin.utils.state.Fallible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ObserveTradePricesUseCaseTest {
    private val classifier = object : ErrorClassifier() {
        override fun isNetworkError(e: Throwable) = false
    }

    @Test
    fun `invoke returns flow from repository`() = runTest {
        val expected = TradePrice("BTCUSDT", PricePoint(1000L, 50000.0))
        val repo = FakeTradePriceRepository(tradeFlow = flowOf(expected))
        val useCase = ObserveTradePricesUseCaseImpl(repo, classifier)

        val result = useCase("BTCUSDT")

        val fallible = result.first()
        val success = fallible as Fallible.Success
        assertEquals(expected, success.value)
    }
}
