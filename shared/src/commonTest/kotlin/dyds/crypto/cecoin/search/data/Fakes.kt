package dyds.crypto.cecoin.search.data

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.search.data.datasource.CoinListDataSource
import dyds.crypto.cecoin.search.data.datasource.FavoriteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeCoinListDataSource(
    var symbols: List<CryptoSymbol> = emptyList(),
    var exception: Throwable? = null,
) : CoinListDataSource {
    var callCount = 0

    override suspend fun fetchSymbols(): List<CryptoSymbol> {
        callCount++
        exception?.let { throw it }
        return symbols
    }
}

internal class FakeFavoriteDataSource(
    initial: Set<CryptoSymbol> = emptySet(),
) : FavoriteDataSource {
    private val _favorites = MutableStateFlow(initial)

    override val favorites: Flow<Set<CryptoSymbol>> = _favorites

    override suspend fun toggle(symbol: CryptoSymbol) {
        _favorites.value = if (symbol in _favorites.value) {
            _favorites.value - symbol
        } else {
            _favorites.value + symbol
        }
    }
}
