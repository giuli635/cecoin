package dyds.crypto.cecoin.data.search.repository

import dyds.crypto.cecoin.data.search.datasource.FavoriteDataSource
import dyds.crypto.cecoin.domain.search.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow

class FavoriteRepositoryImpl(
    private val source: FavoriteDataSource,
) : FavoriteRepository {
    override fun observeFavorites(): Flow<Set<String>> = source.favorites
    override suspend fun toggleFavorite(symbol: String) = source.toggle(symbol)
}
