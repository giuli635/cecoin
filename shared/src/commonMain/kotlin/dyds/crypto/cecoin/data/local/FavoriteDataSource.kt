package dyds.crypto.cecoin.data.local

import kotlinx.coroutines.flow.Flow

interface FavoriteDataSource {
    val favorites: Flow<Set<String>>
    suspend fun toggle(symbol: String)
}
