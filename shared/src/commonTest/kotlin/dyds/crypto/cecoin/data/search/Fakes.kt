package dyds.crypto.cecoin.data.search

import dyds.crypto.cecoin.data.search.datasource.CoinListDataSource
import dyds.crypto.cecoin.data.search.datasource.FavoriteDataSource
import dyds.crypto.cecoin.domain.search.model.CryptoSymbol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeCoinListDataSource(
    private val symbols: List<CryptoSymbol> = emptyList(),
) : CoinListDataSource {
    override suspend fun fetchSymbols(): List<CryptoSymbol> = symbols
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
