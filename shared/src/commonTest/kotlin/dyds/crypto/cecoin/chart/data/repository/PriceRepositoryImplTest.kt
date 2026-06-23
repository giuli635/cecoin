package dyds.crypto.cecoin.chart.data.repository

import dyds.crypto.cecoin.chart.data.FakeCoinHistoricalSource
import dyds.crypto.cecoin.chart.data.FakeCoinPriceSource
import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.utils.fakeBtcSymbol
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PriceRepositoryImplTest {
    private val btcPricePoint = PricePoint(1000L, 50000.0)

    @Test
    fun `getHistoricalPrices delegates to historical source with normalized symbol`() = runTest {
        val historicalSource = FakeCoinHistoricalSource(listOf(btcPricePoint))
        val repo = PriceRepositoryImpl(FakeCoinPriceSource(), historicalSource)

        val result = repo.getHistoricalPrices(CryptoSymbol("  btcusdt  "), "1m", 200)

        assertEquals(listOf(btcPricePoint), result)
        assertEquals(fakeBtcSymbol, historicalSource.lastSymbol)
    }

    @Test
    fun `observePrices delegates to price source with normalized symbol`() = runTest {
        val priceSource = FakeCoinPriceSource(flowOf(btcPricePoint))
        val repo = PriceRepositoryImpl(priceSource, FakeCoinHistoricalSource())

        val result = repo.observePrices(CryptoSymbol("  ETHUSDT  ")).first()

        assertEquals(btcPricePoint, result)
        assertEquals(CryptoSymbol("ETHUSDT"), priceSource.lastSymbol)
    }

    @Test
    fun `getHistoricalPrices uses DefaultSymbol for blank input`() = runTest {
        val historicalSource = FakeCoinHistoricalSource(listOf(btcPricePoint))
        val repo = PriceRepositoryImpl(FakeCoinPriceSource(), historicalSource)

        repo.getHistoricalPrices(CryptoSymbol("   "), "1m", 200)

        assertEquals(fakeBtcSymbol, historicalSource.lastSymbol)
    }

    @Test
    fun `observePrices uses DefaultSymbol for blank input`() = runTest {
        val priceSource = FakeCoinPriceSource(flowOf(btcPricePoint))
        val repo = PriceRepositoryImpl(priceSource, FakeCoinHistoricalSource())

        repo.observePrices(CryptoSymbol("   "))

        assertEquals(fakeBtcSymbol, priceSource.lastSymbol)
    }

    @Test
    fun `getHistoricalPrices propagates source exception`() = runTest {
        val historicalSource = FakeCoinHistoricalSource(exception = RuntimeException("source fail"))
        val repo = PriceRepositoryImpl(FakeCoinPriceSource(), historicalSource)

        assertFailsWith<RuntimeException> {
            repo.getHistoricalPrices(fakeBtcSymbol, "1m", 200)
        }
    }

    @Test
    fun `observePrices propagates source exception`() = runTest {
        val throwingSource = FakeCoinPriceSource(flow { throw RuntimeException("ws fail") })
        val repo = PriceRepositoryImpl(throwingSource, FakeCoinHistoricalSource())

        assertFailsWith<RuntimeException> {
            repo.observePrices(fakeBtcSymbol).first()
        }
    }

    @Test
    fun `getHistoricalPrices uses default interval when not specified`() = runTest {
        val historicalSource = FakeCoinHistoricalSource(listOf(btcPricePoint))
        val repo = PriceRepositoryImpl(FakeCoinPriceSource(), historicalSource)

        repo.getHistoricalPrices(fakeBtcSymbol)

        assertEquals("1m", historicalSource.lastInterval)
    }

    @Test
    fun `historical and price sources work independently`() = runTest {
        val historicalSource = FakeCoinHistoricalSource(listOf(btcPricePoint))
        val priceSource = FakeCoinPriceSource(flowOf(btcPricePoint))
        val repo = PriceRepositoryImpl(priceSource, historicalSource)

        val historical = repo.getHistoricalPrices(fakeBtcSymbol)
        assertTrue(historical.isNotEmpty())

        val price = repo.observePrices(fakeBtcSymbol).first()
        assertEquals(btcPricePoint, price)
    }
}
