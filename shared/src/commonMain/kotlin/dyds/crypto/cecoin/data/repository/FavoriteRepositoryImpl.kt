package dyds.crypto.cecoin.data.repository

import dyds.crypto.cecoin.data.local.FavoriteLocalSource
import dyds.crypto.cecoin.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class FavoriteRepositoryImpl(
    private val source: FavoriteLocalSource,
) : FavoriteRepository {

    override fun observeFavorites(): Flow<Set<String>> = source.favorites

    override suspend fun toggleFavorite(symbol: String) = source.toggle(symbol)

    override fun isFavorite(symbol: String): Flow<Boolean> =
        source.favorites.map { symbol in it }
}
