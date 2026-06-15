package dyds.crypto.cecoin.data

import dyds.crypto.cecoin.data.local.FavoriteStorage
import dyds.crypto.cecoin.data.remote.CoinHistoricalSource
import dyds.crypto.cecoin.data.remote.CoinListDataSource
import dyds.crypto.cecoin.data.remote.CoinPriceSource
import dyds.crypto.cecoin.domain.model.CryptoSymbol
import dyds.crypto.cecoin.domain.model.TradePrice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

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
    private val flow: Flow<TradePrice> = emptyFlow(),
) : CoinPriceSource {
    var lastSymbol: String = ""

    override fun tradePrices(symbol: String): Flow<TradePrice> {
        lastSymbol = symbol
        return flow
    }

    override fun close() {}
}

internal class FakeFavoriteStorage(
    initial: Set<String> = emptySet(),
) : FavoriteStorage {
    private val data = initial.toMutableSet()
    var saved: Set<String> = initial

    override fun load(): Set<String> = data.toSet()
    override fun save(favorites: Set<String>) {
        data.clear()
        data.addAll(favorites)
        saved = data.toSet()
    }
}
