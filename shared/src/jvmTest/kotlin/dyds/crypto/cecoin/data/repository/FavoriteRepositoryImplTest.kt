package dyds.crypto.cecoin.data.repository

import dyds.crypto.cecoin.data.local.FavoriteLocalSource
import dyds.crypto.cecoin.data.local.FavoriteStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FavoriteRepositoryImplTest {
    @Test
    fun `observeFavorites returns empty set by default`() = runTest {
        val storage = FakeFavoriteStorage()
        val source = FavoriteLocalSource(storage)
        val repo = FavoriteRepositoryImpl(source)

        val result = repo.observeFavorites().first()

        assertEquals(emptySet(), result)
    }

    @Test
    fun `toggleFavorite delegates to source`() = runTest {
        val storage = FakeFavoriteStorage()
        val source = FavoriteLocalSource(storage)
        val repo = FavoriteRepositoryImpl(source)

        repo.toggleFavorite("BTCUSDT")

        val result = repo.observeFavorites().first()
        assertEquals(setOf("BTCUSDT"), result)
    }

    @Test
    fun `toggleFavorite removes existing favorite`() = runTest {
        val storage = FakeFavoriteStorage(initial = setOf("BTCUSDT"))
        val source = FavoriteLocalSource(storage)
        val repo = FavoriteRepositoryImpl(source)

        repo.toggleFavorite("BTCUSDT")

        val result = repo.observeFavorites().first()
        assertEquals(emptySet(), result)
    }

    @Test
    fun `observeFavorites returns favorites loaded from storage`() = runTest {
        val storage = FakeFavoriteStorage(initial = setOf("BTCUSDT", "ETHUSDT"))
        val source = FavoriteLocalSource(storage)
        val repo = FavoriteRepositoryImpl(source)

        val result = repo.observeFavorites().first()

        assertEquals(setOf("BTCUSDT", "ETHUSDT"), result)
    }
}

internal class FakeFavoriteStorage(
    var saved: Set<String> = emptySet(),
    initial: Set<String> = emptySet(),
) : FavoriteStorage {
    private val data = initial.toMutableSet()

    override fun load(): Set<String> = data.toSet()

    override fun save(favorites: Set<String>) {
        data.clear()
        data.addAll(favorites)
        saved = data.toSet()
    }
}
