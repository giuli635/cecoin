package dyds.crypto.cecoin.search.data.repository

import dyds.crypto.cecoin.search.data.datasource.FavoriteDataSource
import dyds.crypto.cecoin.search.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow

class FavoriteRepositoryImpl(
    private val source: FavoriteDataSource,
) : FavoriteRepository {
    override fun observeFavorites(): Flow<Set<String>> = source.favorites
    override suspend fun toggleFavorite(symbol: String) = source.toggle(symbol)
}
