package dyds.crypto.cecoin.domain.search.usecase

import dyds.crypto.cecoin.domain.search.FakeFavoriteRepository
import dyds.crypto.cecoin.utils.error.fakeErrorClassifier
import dyds.crypto.cecoin.utils.state.Fallible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ToggleFavoriteUseCaseTest {
    private val classifier = fakeErrorClassifier()

    @Test
    fun `invoke delegates toggle to repository`() = runTest {
        val repo = FakeFavoriteRepository()
        val useCase = ToggleFavoriteUseCaseImpl(repo, classifier)

        val result = useCase("BTCUSDT")

        assertIs<Fallible.Success<*>>(result)
        assertEquals("BTCUSDT", repo.toggledSymbol)
    }

    @Test
    fun `double toggle restores original state`() = runTest {
        val repo = FakeFavoriteRepository()
        val useCase = ToggleFavoriteUseCaseImpl(repo, classifier)

        useCase("BTCUSDT")
        useCase("BTCUSDT")

        val result = repo.observeFavorites().first()
        assertEquals(emptySet(), result)
    }
}
