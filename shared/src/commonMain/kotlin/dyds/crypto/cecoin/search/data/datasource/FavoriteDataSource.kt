package dyds.crypto.cecoin.search.data.datasource

import kotlinx.coroutines.flow.Flow

interface FavoriteDataSource {
    val favorites: Flow<Set<String>>
    suspend fun toggle(symbol: String)
}
