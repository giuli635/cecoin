package dyds.crypto.cecoin.data.local

interface FavoriteStorage {
    fun load(): Set<String>
    fun save(favorites: Set<String>)
}

expect fun createFavoriteStorage(): FavoriteStorage
