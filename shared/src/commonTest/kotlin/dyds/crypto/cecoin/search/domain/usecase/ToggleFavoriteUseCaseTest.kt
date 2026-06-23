package dyds.crypto.cecoin.search.domain.usecase

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.utils.fakeBtcSymbol
import dyds.crypto.cecoin.search.domain.FakeFavoriteRepository
import dyds.crypto.cecoin.core.domain.error.fakeErrorClassifier
import dyds.crypto.cecoin.core.domain.state.Fallible
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

        val result = useCase(fakeBtcSymbol)

        assertIs<Fallible.Success<*>>(result)
        assertEquals(fakeBtcSymbol, repo.toggledSymbol)
    }

    @Test
    fun `double toggle restores original state`() = runTest {
        val repo = FakeFavoriteRepository()
        val useCase = ToggleFavoriteUseCaseImpl(repo, classifier)

        useCase(fakeBtcSymbol)
        useCase(fakeBtcSymbol)

        val result = repo.observeFavorites().first()
        assertEquals(emptySet(), result)
    }

    @Test
    fun `invoke returns Failed when repository throws`() = runTest {
        val repo = FakeFavoriteRepository(exception = RuntimeException("repo fail"))
        val useCase = ToggleFavoriteUseCaseImpl(repo, classifier, lazyMessage = { "repo fail" })

        val result = useCase(fakeBtcSymbol)

        assertIs<Fallible.Failed>(result)
    }
}
