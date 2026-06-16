package dyds.crypto.cecoin.data.repository

import dyds.crypto.cecoin.data.FakeFavoriteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FavoriteRepositoryImplTest {
    @Test
    fun `observeFavorites returns empty set by default`() = runTest {
        val source = FakeFavoriteDataSource()
        val repo = FavoriteRepositoryImpl(source)

        val result = repo.observeFavorites().first()

        assertEquals(emptySet(), result)
    }

    @Test
    fun `toggleFavorite delegates to source`() = runTest {
        val source = FakeFavoriteDataSource()
        val repo = FavoriteRepositoryImpl(source)

        repo.toggleFavorite("BTCUSDT")

        val result = repo.observeFavorites().first()
        assertEquals(setOf("BTCUSDT"), result)
    }

    @Test
    fun `toggleFavorite removes existing favorite`() = runTest {
        val source = FakeFavoriteDataSource(initial = setOf("BTCUSDT"))
        val repo = FavoriteRepositoryImpl(source)

        repo.toggleFavorite("BTCUSDT")

        val result = repo.observeFavorites().first()
        assertEquals(emptySet(), result)
    }

    @Test
    fun `observeFavorites returns favorites loaded from storage`() = runTest {
        val source = FakeFavoriteDataSource(initial = setOf("BTCUSDT", "ETHUSDT"))
        val repo = FavoriteRepositoryImpl(source)

        val result = repo.observeFavorites().first()

        assertEquals(setOf("BTCUSDT", "ETHUSDT"), result)
    }
}
