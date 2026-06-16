package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.FakeFavoriteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveFavoritesUseCaseTest {
    @Test
    fun `invoke returns favorites flow from repository`() = runTest {
        val expected = setOf("BTCUSDT", "ETHUSDT")
        val repo = FakeFavoriteRepository(initialFavorites = expected)
        val useCase = ObserveFavoritesUseCase(repo)

        val result = useCase()

        assertEquals(expected, result.first())
    }
}
