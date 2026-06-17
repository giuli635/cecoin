package dyds.crypto.cecoin.domain.news.usecase

import dyds.crypto.cecoin.domain.news.FakeNewsRepository
import dyds.crypto.cecoin.domain.news.model.NewsArticle
import dyds.crypto.cecoin.utils.error.ErrorClassifier
import dyds.crypto.cecoin.utils.state.Fallible
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetCryptoNewsUseCaseTest {
    private val classifier = object : ErrorClassifier() {
        override fun isNetworkError(e: Throwable) = false
    }

    @Test
    fun `invoke returns articles from repository`() = runTest {
        val expected = listOf(
            NewsArticle("Title", "Desc", "url", null, "Source", "2024-01-01"),
        )
        val repo = FakeNewsRepository(articles = expected)
        val useCase = GetCryptoNewsUseCaseImpl(repo, classifier)

        val result = useCase()

        val success = result as Fallible.Success
        assertEquals(expected, success.value)
    }

    @Test
    fun `invoke returns empty list when repository returns empty`() = runTest {
        val repo = FakeNewsRepository()
        val useCase = GetCryptoNewsUseCaseImpl(repo, classifier)

        val result = useCase()

        val success = result as Fallible.Success
        assertEquals(0, success.value.size)
    }

    @Test
    fun `invoke returns Failed when repository throws`() = runTest {
        val repo = FakeNewsRepository(exception = RuntimeException("repo fail"))
        val useCase = GetCryptoNewsUseCaseImpl(repo, classifier)

        val result = useCase()

        assertIs<Fallible.Failed>(result)
    }
}
