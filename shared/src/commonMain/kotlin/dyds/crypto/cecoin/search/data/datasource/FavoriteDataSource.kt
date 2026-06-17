package dyds.crypto.cecoin.search.data.datasource

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import kotlinx.coroutines.flow.Flow

interface FavoriteDataSource {
    val favorites: Flow<Set<CryptoSymbol>>
    suspend fun toggle(symbol: CryptoSymbol)
}
