package dyds.crypto.cecoin.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun observeFavorites(): Flow<Set<String>>
    suspend fun toggleFavorite(symbol: String)
    fun isFavorite(symbol: String): Flow<Boolean>
}
