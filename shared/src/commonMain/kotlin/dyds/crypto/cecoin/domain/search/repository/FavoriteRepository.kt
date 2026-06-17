package dyds.crypto.cecoin.domain.search.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun observeFavorites(): Flow<Set<String>>
    suspend fun toggleFavorite(symbol: String)
}
