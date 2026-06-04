package dyds.crypto.cecoin.data.local

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
private data class FavoritesData(val symbols: List<String>)

private val favoritesFile = File(System.getProperty("user.home"), ".cecoin/favorites.json")
private val json = Json { ignoreUnknownKeys = true }

actual fun loadFavoritesFromDisk(): Set<String> {
    if (!favoritesFile.exists()) return emptySet()
    return runCatching {
        val text = favoritesFile.readText()
        json.decodeFromString<FavoritesData>(text).symbols.toSet()
    }.getOrDefault(emptySet())
}

actual fun saveFavoritesToDisk(favorites: Set<String>) {
    favoritesFile.parentFile.mkdirs()
    val text = json.encodeToString(FavoritesData(favorites.toList()))
    favoritesFile.writeText(text)
}
