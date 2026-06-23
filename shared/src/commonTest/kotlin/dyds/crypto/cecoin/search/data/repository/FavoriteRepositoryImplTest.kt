package dyds.crypto.cecoin.search.data.repository

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.utils.fakeBtcSymbol
import dyds.crypto.cecoin.core.utils.fakeEthSymbol
import dyds.crypto.cecoin.search.data.FakeFavoriteDataSource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FavoriteRepositoryImplTest {
    private fun createRepo(initial: Set<CryptoSymbol> = emptySet()): FavoriteRepositoryImpl =
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

        repo.toggleFavorite(fakeBtcSymbol)

        val result = repo.observeFavorites().first()
        assertEquals(setOf(fakeBtcSymbol), result)
    }

    @Test
    fun `toggleFavorite removes existing favorite`() = runTest {
        val repo = createRepo(initial = setOf(fakeBtcSymbol))

        repo.toggleFavorite(fakeBtcSymbol)

        val result = repo.observeFavorites().first()
        assertEquals(emptySet(), result)
    }

    @Test
    fun `observeFavorites returns favorites loaded from storage`() = runTest {
        val repo = createRepo(initial = setOf(fakeBtcSymbol, fakeEthSymbol))

        val result = repo.observeFavorites().first()

        assertEquals(setOf(fakeBtcSymbol, fakeEthSymbol), result)
    }

    @Test
    fun `toggleFavorite with empty symbol does not crash`() = runTest {
        val repo = createRepo()

        repo.toggleFavorite(CryptoSymbol(""))

        val result = repo.observeFavorites().first()
        assertEquals(setOf(CryptoSymbol("")), result)
    }

    @Test
    fun `concurrent toggles do not cause data loss`() = runTest {
        val repo = createRepo()
        val set = fakeBtcSymbol
        val other = fakeEthSymbol

        coroutineScope {
            launch { repo.toggleFavorite(set) }
            launch { repo.toggleFavorite(other) }
        }

        val favs = repo.observeFavorites().first()
        assertEquals(setOf(set, other), favs)
    }

    @Test
    fun `toggleFavorite on non-existent symbol adds it`() = runTest {
        val repo = createRepo()

        repo.toggleFavorite(fakeBtcSymbol)

        val favs = repo.observeFavorites().first()
        assertTrue(favs.contains(fakeBtcSymbol))
    }
}
