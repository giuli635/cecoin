package dyds.crypto.cecoin.data.repository

import dyds.crypto.cecoin.data.local.FavoriteLocalSource
import dyds.crypto.cecoin.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow

class FavoriteRepositoryImpl(
    private val source: FavoriteLocalSource,
) : FavoriteRepository {
    override fun observeFavorites(): Flow<Set<String>> = source.favorites
    override suspend fun toggleFavorite(symbol: String) = source.toggle(symbol)
}
