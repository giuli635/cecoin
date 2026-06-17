package dyds.crypto.cecoin.search.domain.repository

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun observeFavorites(): Flow<Set<CryptoSymbol>>
    suspend fun toggleFavorite(symbol: CryptoSymbol)
}
