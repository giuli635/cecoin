package dyds.crypto.cecoin.data.repository

import dyds.crypto.cecoin.data.remote.CoinHistoricalSource
import dyds.crypto.cecoin.data.remote.CoinListDataSource
import dyds.crypto.cecoin.data.remote.CoinPriceSource
import dyds.crypto.cecoin.domain.model.CryptoSymbol
import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.domain.model.TradePrice
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CecoinRepositoryImplTest {
    private val btcTrade = TradePrice("BTCUSDT", PricePoint(1000L, 50000.0))
    private val ethTrade = TradePrice("ETHUSDT", PricePoint(2000L, 3000.0))
    private val btcSymbol = CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING")

    @Test
    fun `getAvailableSymbols delegates to list data source`() = runTest {
        val listSource = FakeCoinListDataSource(listOf(btcSymbol))
        val repo = CecoinRepositoryImpl(FakeCoinPriceSource(), FakeCoinHistoricalSource(), listSource)

        val result = repo.getAvailableSymbols()

        assertEquals(listOf(btcSymbol), result)
    }

    @Test
    fun `getHistoricalPrices delegates to historical source with normalized symbol`() = runTest {
        val historicalSource = FakeCoinHistoricalSource(listOf(btcTrade))
        val repo = CecoinRepositoryImpl(FakeCoinPriceSource(), historicalSource, FakeCoinListDataSource())

        val result = repo.getHistoricalPrices("  btcusdt  ", "1m", 200)

        assertEquals(listOf(btcTrade), result)
        assertEquals("BTCUSDT", historicalSource.lastSymbol)
    }

    @Test
    fun `observeTradePrices delegates to price source with normalized symbol`() = runTest {
        val priceSource = FakeCoinPriceSource(flowOf(btcTrade))
        val repo = CecoinRepositoryImpl(priceSource, FakeCoinHistoricalSource(), FakeCoinListDataSource())

        val result = repo.observeTradePrices("  ETHUSDT  ").first()

        assertEquals(btcTrade, result)
        assertEquals("ETHUSDT", priceSource.lastSymbol)
    }

    @Test
    fun `getHistoricalPrices uses DefaultSymbol for blank input`() = runTest {
        val historicalSource = FakeCoinHistoricalSource(listOf(btcTrade))
        val repo = CecoinRepositoryImpl(FakeCoinPriceSource(), historicalSource, FakeCoinListDataSource())

        repo.getHistoricalPrices("   ", "1m", 200)

        assertEquals("BTCUSDT", historicalSource.lastSymbol)
    }

    @Test
    fun `observeTradePrices uses DefaultSymbol for blank input`() {
        val priceSource = FakeCoinPriceSource(flowOf(btcTrade))
        val repo = CecoinRepositoryImpl(priceSource, FakeCoinHistoricalSource(), FakeCoinListDataSource())

        repo.observeTradePrices("   ")

        assertEquals("BTCUSDT", priceSource.lastSymbol)
    }

    @Test
    fun `getAvailableSymbols returns empty list when source returns empty`() = runTest {
        val listSource = FakeCoinListDataSource(emptyList())
        val repo = CecoinRepositoryImpl(FakeCoinPriceSource(), FakeCoinHistoricalSource(), listSource)

        val result = repo.getAvailableSymbols()

        assertTrue(result.isEmpty())
    }
}

// --- Fakes ---

internal class FakeCoinListDataSource(
    private val symbols: List<CryptoSymbol> = emptyList(),
) : CoinListDataSource {
    override suspend fun fetchSymbols(): List<CryptoSymbol> = symbols
    override fun close() {}
}

internal class FakeCoinHistoricalSource(
    private val prices: List<TradePrice> = emptyList(),
) : CoinHistoricalSource {
    var lastSymbol: String = ""
    var lastInterval: String = ""
    var lastLimit: Int = 0

    override suspend fun getHistoricalPrices(
        symbol: String, interval: String, limit: Int,
    ): List<TradePrice> {
        lastSymbol = symbol
        lastInterval = interval
        lastLimit = limit
        return prices
    }

    override fun close() {}
}

internal class FakeCoinPriceSource(
    private val flow: kotlinx.coroutines.flow.Flow<TradePrice> = kotlinx.coroutines.flow.emptyFlow(),
) : CoinPriceSource {
    var lastSymbol: String = ""

    override fun tradePrices(symbol: String): kotlinx.coroutines.flow.Flow<TradePrice> {
        lastSymbol = symbol
        return flow
    }

    override fun close() {}
}
