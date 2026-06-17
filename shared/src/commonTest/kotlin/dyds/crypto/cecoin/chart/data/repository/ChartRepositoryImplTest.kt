package dyds.crypto.cecoin.chart.data.repository

import dyds.crypto.cecoin.chart.data.FakeCoinHistoricalSource
import dyds.crypto.cecoin.chart.data.FakeCoinPriceSource
import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.domain.model.TradePrice
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ChartRepositoryImplTest {
    private val btcTrade = TradePrice("BTCUSDT", PricePoint(1000L, 50000.0))
    private val ethTrade = TradePrice("ETHUSDT", PricePoint(2000L, 3000.0))

    @Test
    fun `getHistoricalPrices delegates to historical source with normalized symbol`() = runTest {
        val historicalSource = FakeCoinHistoricalSource(listOf(btcTrade))
        val repo = ChartRepositoryImpl(FakeCoinPriceSource(), historicalSource)

        val result = repo.getHistoricalPrices("  btcusdt  ", "1m", 200)

        assertEquals(listOf(btcTrade), result)
        assertEquals("BTCUSDT", historicalSource.lastSymbol)
    }

    @Test
    fun `observeTradePrices delegates to price source with normalized symbol`() = runTest {
        val priceSource = FakeCoinPriceSource(flowOf(btcTrade))
        val repo = ChartRepositoryImpl(priceSource, FakeCoinHistoricalSource())

        val result = repo.observeTradePrices("  ETHUSDT  ").first()

        assertEquals(btcTrade, result)
        assertEquals("ETHUSDT", priceSource.lastSymbol)
    }

    @Test
    fun `getHistoricalPrices uses DefaultSymbol for blank input`() = runTest {
        val historicalSource = FakeCoinHistoricalSource(listOf(btcTrade))
        val repo = ChartRepositoryImpl(FakeCoinPriceSource(), historicalSource)

        repo.getHistoricalPrices("   ", "1m", 200)

        assertEquals("BTCUSDT", historicalSource.lastSymbol)
    }

    @Test
    fun `observeTradePrices uses DefaultSymbol for blank input`() = runTest {
        val priceSource = FakeCoinPriceSource(flowOf(btcTrade))
        val repo = ChartRepositoryImpl(priceSource, FakeCoinHistoricalSource())

        repo.observeTradePrices("   ")

        assertEquals("BTCUSDT", priceSource.lastSymbol)
    }

    @Test
    fun `getHistoricalPrices propagates source exception`() = runTest {
        val historicalSource = FakeCoinHistoricalSource(exception = RuntimeException("source fail"))
        val repo = ChartRepositoryImpl(FakeCoinPriceSource(), historicalSource)

        assertFailsWith<RuntimeException> {
            repo.getHistoricalPrices("BTCUSDT", "1m", 200)
        }
    }
}
