package dyds.crypto.cecoin.search.data

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.search.data.datasource.CoinListDataSource
import dyds.crypto.cecoin.search.data.datasource.FavoriteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeCoinListDataSource(
    private val symbols: List<CryptoSymbol> = emptyList(),
) : CoinListDataSource {
    override suspend fun fetchSymbols(): List<CryptoSymbol> = symbols
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
