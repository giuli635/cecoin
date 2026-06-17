package dyds.crypto.cecoin.search.data.repository

import dyds.crypto.cecoin.search.data.FakeFavoriteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FavoriteRepositoryImplTest {
    private fun createRepo(initial: Set<String> = emptySet()): FavoriteRepositoryImpl =
        FavoriteRepositoryImpl(FakeFavoriteDataSource(initial = initial))

    @Test
    fun `observeFavorites returns empty set by default`() = runTest {
        val repo = createRepo()

        val result = repo.observeFavorites().first()

        assertEquals(emptySet(), result)
    }

    @Test
    fun `toggleFavorite delegates to source`() = runTest {
        val repo = createRepo()

        repo.toggleFavorite("BTCUSDT")

        val result = repo.observeFavorites().first()
        assertEquals(setOf("BTCUSDT"), result)
    }

    @Test
    fun `toggleFavorite removes existing favorite`() = runTest {
        val repo = createRepo(initial = setOf("BTCUSDT"))

        repo.toggleFavorite("BTCUSDT")

        val result = repo.observeFavorites().first()
        assertEquals(emptySet(), result)
    }

    @Test
    fun `observeFavorites returns favorites loaded from storage`() = runTest {
        val repo = createRepo(initial = setOf("BTCUSDT", "ETHUSDT"))

        val result = repo.observeFavorites().first()

        assertEquals(setOf("BTCUSDT", "ETHUSDT"), result)
    }

    @Test
    fun `toggleFavorite with empty symbol does not crash`() = runTest {
        val repo = createRepo()

        repo.toggleFavorite("")

        val result = repo.observeFavorites().first()
        assertEquals(setOf(""), result)
    }
}
