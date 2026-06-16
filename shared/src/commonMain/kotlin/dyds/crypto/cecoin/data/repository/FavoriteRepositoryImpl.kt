package dyds.crypto.cecoin.data.repository

import dyds.crypto.cecoin.data.local.FavoriteDataSource
import dyds.crypto.cecoin.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow

class FavoriteRepositoryImpl(
    private val source: FavoriteDataSource,
) : FavoriteRepository {
    override fun observeFavorites(): Flow<Set<String>> = source.favorites
    override suspend fun toggleFavorite(symbol: String) = source.toggle(symbol)
}
