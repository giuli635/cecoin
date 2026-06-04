package dyds.crypto.cecoin.data.local

expect fun loadFavoritesFromDisk(): Set<String>

expect fun saveFavoritesToDisk(favorites: Set<String>)
