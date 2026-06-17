package dyds.crypto.cecoin.search.domain.usecase

import dyds.crypto.cecoin.search.domain.FakeFavoriteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveFavoritesUseCaseTest {
    @Test
    fun `invoke returns favorites flow from repository`() = runTest {
        val expected = setOf("BTCUSDT", "ETHUSDT")
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
        repo.toggleFavorite("BTCUSDT")
        val emitted = flow.first { it == setOf("BTCUSDT") }

        assertEquals(setOf("BTCUSDT"), emitted)
    }
}
