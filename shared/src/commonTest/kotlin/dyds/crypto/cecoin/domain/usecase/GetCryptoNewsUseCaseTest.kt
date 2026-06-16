package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.FakeNewsRepository
import dyds.crypto.cecoin.domain.model.NewsArticle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetCryptoNewsUseCaseTest {
    @Test
    fun `invoke returns articles from repository`() = runTest {
        val expected = listOf(
            NewsArticle("Title", "Desc", "url", null, "Source", "2024-01-01"),
        )
        val repo = FakeNewsRepository(articles = expected)
        val useCase = GetCryptoNewsUseCase(repo)

        val result = useCase()

        assertEquals(expected, result)
    }

    @Test
    fun `invoke returns empty list when repository returns empty`() = runTest {
        val repo = FakeNewsRepository()
        val useCase = GetCryptoNewsUseCase(repo)

        val result = useCase()

        assertEquals(0, result.size)
    }

    @Test
    fun `invoke propagates repository exception`() = runTest {
        val repo = FakeNewsRepository(exception = RuntimeException("repo fail"))
        val useCase = GetCryptoNewsUseCase(repo)

        assertFailsWith<RuntimeException> {
            useCase()
        }
    }
}
