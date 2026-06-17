package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.model.CryptoSymbol
import dyds.crypto.cecoin.domain.model.NewsArticle
import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.utils.AppError
import dyds.crypto.cecoin.utils.Fallible
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGetHistoricalPricesUseCase(
    var prices: List<TradePrice> = emptyList(),
    var exception: Throwable? = null,
) : GetHistoricalPricesUseCase {
    var lastSymbol: String = ""
    var lastInterval: String = ""
    var lastLimit: Int = 0

    override suspend fun invoke(symbol: String, interval: String, limit: Int): Fallible<List<TradePrice>> {
        lastSymbol = symbol
        lastInterval = interval
        lastLimit = limit
        if (exception != null) return Fallible.Failed(AppError.GenericError(exception!!, "fallo"))
        return Fallible.Success(prices)
    }
}

class FakeGetAvailableSymbolsUseCase(
    var symbols: List<CryptoSymbol> = emptyList(),
    var exception: Throwable? = null,
) : GetAvailableSymbolsUseCase {
    override suspend fun invoke(): Fallible<List<CryptoSymbol>> {
        if (exception != null) return Fallible.Failed(AppError.GenericError(exception!!, "fallo"))
        return Fallible.Success(symbols)
    }
}

class FakeGetCryptoNewsUseCase(
    var articles: List<NewsArticle> = emptyList(),
    var exception: Throwable? = null,
) : GetCryptoNewsUseCase {
    override suspend fun invoke(): Fallible<List<NewsArticle>> {
        if (exception != null) return Fallible.Failed(AppError.GenericError(exception!!, "fallo"))
        return Fallible.Success(articles)
    }
}

class FakeObserveFavoritesUseCase(
    initial: Set<String> = emptySet(),
    private val flow: MutableStateFlow<Set<String>> = MutableStateFlow(initial),
) : ObserveFavoritesUseCase {
    override fun invoke(): Flow<Set<String>> = flow
}

class FakeToggleFavoriteUseCase(
    val favorites: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet()),
) : ToggleFavoriteUseCase {
    var lastToggled: String? = null

    override suspend fun invoke(symbol: String): Fallible<Unit> {
        lastToggled = symbol
        favorites.value = if (symbol in favorites.value) {
            favorites.value - symbol
        } else {
            favorites.value + symbol
        }
        return Fallible.Success(Unit)
    }
}
