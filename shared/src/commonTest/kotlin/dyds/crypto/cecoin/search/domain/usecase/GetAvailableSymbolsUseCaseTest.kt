package dyds.crypto.cecoin.search.domain.usecase

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.utils.fakeBtcSymbol
import dyds.crypto.cecoin.search.domain.FakeCryptoSymbolRepository
import dyds.crypto.cecoin.core.domain.error.fakeErrorClassifier
import dyds.crypto.cecoin.core.domain.state.Fallible
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetAvailableSymbolsUseCaseTest {
    private val classifier = fakeErrorClassifier()

    @Test
    fun `invoke returns symbols from repository`() = runTest {
        val expected = listOf(fakeBtcSymbol)
        val repo = FakeCryptoSymbolRepository(expected)
        val useCase = GetAvailableSymbolsUseCaseImpl(repo, classifier)

        val result = useCase()

        val success = assertIs<Fallible.Success<List<CryptoSymbol>>>(result)
        assertEquals(expected, success.value)
    }

    @Test
    fun `invoke returns empty list when repository returns empty`() = runTest {
        val repo = FakeCryptoSymbolRepository(emptyList())
        val useCase = GetAvailableSymbolsUseCaseImpl(repo, classifier)

        val result = useCase()

        val success = assertIs<Fallible.Success<List<*>>>(result)
        assertEquals(0, success.value.size)
    }

    @Test
    fun `invoke returns Failed when repository throws`() = runTest {
        val repo = FakeCryptoSymbolRepository(exception = RuntimeException("repo fail"))
        val useCase = GetAvailableSymbolsUseCaseImpl(repo, classifier, lazyMessage = { "repo fail" })

        val result = useCase()

        assertIs<Fallible.Failed>(result)
    }
}
