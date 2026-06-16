package dyds.crypto.cecoin.data

import dyds.crypto.cecoin.data.local.FavoriteDataSource
import dyds.crypto.cecoin.data.remote.CoinHistoricalSource
import dyds.crypto.cecoin.data.remote.CoinListDataSource
import dyds.crypto.cecoin.data.remote.CoinPriceSource
import dyds.crypto.cecoin.data.remote.NewsApiDataSource
import dyds.crypto.cecoin.domain.model.CryptoSymbol
import dyds.crypto.cecoin.domain.model.NewsArticle
import dyds.crypto.cecoin.domain.model.TradePrice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

internal class FakeNewsApiDataSource(
    private val articles: List<NewsArticle> = emptyList(),
) : NewsApiDataSource {
    override suspend fun fetchCryptoNews(): List<NewsArticle> = articles
    override fun close() {}
}

internal class FakeFavoriteDataSource(
    initial: Set<String> = emptySet(),
) : FavoriteDataSource {
    private val _favorites = MutableStateFlow(initial)

    override val favorites: Flow<Set<String>> = _favorites

    override suspend fun toggle(symbol: String) {
        _favorites.value = if (symbol in _favorites.value) {
            _favorites.value - symbol
        } else {
            _favorites.value + symbol
        }
    }
}
