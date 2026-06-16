package dyds.crypto.cecoin.domain

import dyds.crypto.cecoin.domain.model.CryptoSymbol
import dyds.crypto.cecoin.domain.model.NewsArticle
import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.repository.CryptoSymbolRepository
import dyds.crypto.cecoin.domain.repository.FavoriteRepository
import dyds.crypto.cecoin.domain.repository.NewsRepository
import dyds.crypto.cecoin.domain.repository.TradePriceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

internal class FakeCryptoSymbolRepository(
    symbols: List<CryptoSymbol> = emptyList(),
    var exception: Throwable? = null,
) : CryptoSymbolRepository {
    var symbols: List<CryptoSymbol> = symbols

    override suspend fun getAvailableSymbols(): List<CryptoSymbol> {
        exception?.let { throw it }
        return symbols
    }
}

internal class FakeTradePriceRepository(
    historical: List<TradePrice> = emptyList(),
    tradeFlow: Flow<TradePrice> = emptyFlow(),
    var historicalException: Throwable? = null,
) : TradePriceRepository {
    var historical: List<TradePrice> = historical
    var tradeFlow: Flow<TradePrice> = tradeFlow
    var lastSymbol: String = ""
    var lastInterval: String = ""
    var lastLimit: Int = 0

    override suspend fun getHistoricalPrices(
        symbol: String, interval: String, limit: Int,
    ): List<TradePrice> {
        lastSymbol = symbol
        lastInterval = interval
        lastLimit = limit
        historicalException?.let { throw it }
        return historical
    }

    override fun observeTradePrices(symbol: String): Flow<TradePrice> {
        lastSymbol = symbol
        return tradeFlow
    }
}

internal class FakeNewsRepository(
    articles: List<NewsArticle> = emptyList(),
    var exception: Throwable? = null,
) : NewsRepository {
    var articles: List<NewsArticle> = articles

    override suspend fun getCryptoNews(): List<NewsArticle> {
        exception?.let { throw it }
        return articles
    }
}

internal class FakeFavoriteRepository(
    initialFavorites: Set<String> = emptySet(),
) : FavoriteRepository {
    private val favoritesFlow = MutableStateFlow(initialFavorites)
    var toggledSymbol: String? = null

    override fun observeFavorites(): Flow<Set<String>> = favoritesFlow

    override suspend fun toggleFavorite(symbol: String) {
        toggledSymbol = symbol
        favoritesFlow.value = if (symbol in favoritesFlow.value) {
            favoritesFlow.value - symbol
        } else {
            favoritesFlow.value + symbol
        }
    }
}
