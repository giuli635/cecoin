package dyds.crypto.cecoin.search.domain.usecase

import dyds.crypto.cecoin.core.utils.fakeBtcSymbol
import dyds.crypto.cecoin.core.utils.fakeEthSymbol
import dyds.crypto.cecoin.search.domain.FakeFavoriteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveFavoritesUseCaseTest {
    @Test
    fun `invoke returns favorites flow from repository`() = runTest {
        val expected = setOf(fakeBtcSymbol, fakeEthSymbol)
        val repo = FakeFavoriteRepository(initialFavorites = expected)
        val useCase = ObserveFavoritesUseCaseImpl(repo)

        val result = useCase()

        assertEquals(expected, result.first())
    }

    @Test
    fun `invoke emits new value after toggle`() = runTest {
        val repo = FakeFavoriteRepository(initialFavorites = emptySet())
        val useCase = ObserveFavoritesUseCaseImpl(repo)

        val flow = useCase()
        repo.toggleFavorite(fakeBtcSymbol)
        val emitted = flow.first { it == setOf(fakeBtcSymbol) }

        assertEquals(setOf(fakeBtcSymbol), emitted)
    }

    @Test
    fun `invoke emits empty set when no favorites`() = runTest {
        val repo = FakeFavoriteRepository(initialFavorites = emptySet())
        val useCase = ObserveFavoritesUseCaseImpl(repo)

        assertTrue(useCase().first().isEmpty())
    }

    @Test
    fun `invoke emits correct values after multiple toggles`() = runTest {
        val repo = FakeFavoriteRepository()
        val useCase = ObserveFavoritesUseCaseImpl(repo)
        val flow = useCase()

        assertEquals(emptySet(), flow.first())

        repo.toggleFavorite(fakeBtcSymbol)
        assertEquals(setOf(fakeBtcSymbol), flow.first { it == setOf(fakeBtcSymbol) })

        repo.toggleFavorite(fakeEthSymbol)
        assertEquals(setOf(fakeBtcSymbol, fakeEthSymbol), flow.first { it == setOf(fakeBtcSymbol, fakeEthSymbol) })

        repo.toggleFavorite(fakeBtcSymbol)
        assertEquals(setOf(fakeEthSymbol), flow.first { it == setOf(fakeEthSymbol) })
    }
}
