package dyds.crypto.cecoin.data.search.datasource

import kotlinx.coroutines.flow.Flow

interface FavoriteDataSource {
    val favorites: Flow<Set<String>>
    suspend fun toggle(symbol: String)
}
