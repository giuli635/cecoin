package dyds.crypto.cecoin.search.domain

import dyds.crypto.cecoin.search.domain.model.CryptoSymbol
import dyds.crypto.cecoin.search.domain.repository.CryptoSymbolRepository
import dyds.crypto.cecoin.search.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeCryptoSymbolRepository(
    var symbols: List<CryptoSymbol> = emptyList(),
    var exception: Throwable? = null,
) : CryptoSymbolRepository {

    override suspend fun getAvailableSymbols(): List<CryptoSymbol> {
        exception?.let { throw it }
        return symbols
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
