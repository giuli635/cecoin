package dyds.crypto.cecoin.search.data.repository

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.search.data.datasource.FavoriteDataSource
import dyds.crypto.cecoin.search.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow

class FavoriteRepositoryImpl(
    private val source: FavoriteDataSource,
) : FavoriteRepository {
    override fun observeFavorites(): Flow<Set<CryptoSymbol>> = source.favorites
    override suspend fun toggleFavorite(symbol: CryptoSymbol) = source.toggle(symbol)
}
