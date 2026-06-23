package dyds.crypto.cecoin.chart.data.repository

import dyds.crypto.cecoin.chart.data.FakeCoinHistoricalSource
import dyds.crypto.cecoin.chart.data.FakeCoinPriceSource
import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.utils.fakeBtcSymbol
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChartRepositoryBrokerTest {
    @Test
    fun `historical and price sources work independently through repository`() = runTest {
        val historicalSource = FakeCoinHistoricalSource(listOf(PricePoint(1000L, 50000.0)))
        val priceSource = FakeCoinPriceSource(flowOf(PricePoint(1000L, 50000.0)))
        val repo = ChartRepositoryImpl(priceSource, historicalSource)

        val historical = repo.getHistoricalPrices(fakeBtcSymbol)
        assertTrue(historical.isNotEmpty())

        val trade = repo.observePrices(fakeBtcSymbol).first()
        assertEquals(50000.0, trade.price)
    }

    @Test
    fun `symbol normalization applies to both sources`() = runTest {
        val historicalSource = FakeCoinHistoricalSource(listOf(PricePoint(1000L, 50000.0)))
        val priceSource = FakeCoinPriceSource(flowOf(PricePoint(1000L, 50000.0)))
        val repo = ChartRepositoryImpl(priceSource, historicalSource)

        val historical = repo.getHistoricalPrices(CryptoSymbol("  btcusdt  "))
        assertTrue(historical.isNotEmpty())

        val trade = repo.observePrices(CryptoSymbol("  btcusdt  ")).first()
        assertEquals(50000.0, trade.price)
    }
}
