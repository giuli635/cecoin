package dyds.crypto.cecoin.data.local

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
private data class FavoritesData(val symbols: List<String>)

class JvmFavoriteStorage(
    private val file: File = File(System.getProperty("user.home"), ".cecoin/favorites.json"),
) : FavoriteStorage {

    private val json = Json { ignoreUnknownKeys = true }

    override fun load(): Set<String> {
        if (!file.exists()) return emptySet()
        return runCatching {
            val text = file.readText()
            json.decodeFromString<FavoritesData>(text).symbols.toSet()
        }.getOrDefault(emptySet())
    }

    override fun save(favorites: Set<String>) {
        file.parentFile.mkdirs()
        val text = json.encodeToString(FavoritesData(favorites.toList()))
        file.writeText(text)
    }
}

actual fun createFavoriteStorage(): FavoriteStorage = JvmFavoriteStorage()
