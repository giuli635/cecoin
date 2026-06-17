package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.FakeFavoriteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ToggleFavoriteUseCaseTest {
    @Test
    fun `invoke delegates toggle to repository`() = runTest {
        val repo = FakeFavoriteRepository()
        val useCase = ToggleFavoriteUseCaseImpl(repo)

        useCase("BTCUSDT")

        assertEquals("BTCUSDT", repo.toggledSymbol)
    }

    @Test
    fun `double toggle restores original state`() = runTest {
        val repo = FakeFavoriteRepository()
        val useCase = ToggleFavoriteUseCaseImpl(repo)

        useCase("BTCUSDT")
        useCase("BTCUSDT")

        val result = repo.observeFavorites().first()
        assertEquals(emptySet(), result)
    }
}
