package dyds.crypto.cecoin.news.domain.usecase

import dyds.crypto.cecoin.news.domain.FakeNewsRepository
import dyds.crypto.cecoin.news.domain.model.NewsArticle
import dyds.crypto.cecoin.core.domain.error.fakeErrorClassifier
import dyds.crypto.cecoin.core.domain.state.Fallible
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetCryptoNewsUseCaseTest {
    private val classifier = fakeErrorClassifier()

    @Test
    fun `invoke returns articles from repository`() = runTest {
        val expected = listOf(
            NewsArticle("Title", "Desc", "url", null, "Source", "2024-01-01"),
        )
        val repo = FakeNewsRepository(articles = expected)
        val useCase = GetCryptoNewsUseCaseImpl(repo, classifier, lazyMessage = { "test" })

        val result = useCase()

        val success = assertIs<Fallible.Success<List<NewsArticle>>>(result)
        assertEquals(expected, success.value)
    }

    @Test
    fun `invoke returns empty list when repository returns empty`() = runTest {
        val repo = FakeNewsRepository()
        val useCase = GetCryptoNewsUseCaseImpl(repo, classifier, lazyMessage = { "test" })

        val result = useCase()

        val success = assertIs<Fallible.Success<List<*>>>(result)
        assertEquals(0, success.value.size)
    }

    @Test
    fun `invoke returns Failed when repository throws`() = runTest {
        val repo = FakeNewsRepository(exception = RuntimeException("repo fail"))
        val useCase = GetCryptoNewsUseCaseImpl(repo, classifier, lazyMessage = { "repo fail" })

        val result = useCase()

        assertIs<Fallible.Failed>(result)
    }
}
