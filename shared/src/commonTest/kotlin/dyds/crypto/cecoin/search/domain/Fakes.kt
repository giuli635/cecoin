package dyds.crypto.cecoin.search.domain

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
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
    initialFavorites: Set<CryptoSymbol> = emptySet(),
) : FavoriteRepository {
    private val favoritesFlow = MutableStateFlow(initialFavorites)
    var toggledSymbol: CryptoSymbol? = null

    override fun observeFavorites(): Flow<Set<CryptoSymbol>> = favoritesFlow

    override suspend fun toggleFavorite(symbol: CryptoSymbol) {
        toggledSymbol = symbol
        favoritesFlow.value = if (symbol in favoritesFlow.value) {
            favoritesFlow.value - symbol
        } else {
            favoritesFlow.value + symbol
        }
    }
}
