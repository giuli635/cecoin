package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.FakeFavoriteRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ToggleFavoriteUseCaseTest {
    @Test
    fun `invoke delegates toggle to repository`() = runTest {
        val repo = FakeFavoriteRepository()
        val useCase = ToggleFavoriteUseCase(repo)

        useCase("BTCUSDT")

        assertEquals("BTCUSDT", repo.toggledSymbol)
    }
}
