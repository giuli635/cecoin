package dyds.crypto.cecoin.news.presentation

import dyds.crypto.cecoin.news.domain.model.NewsArticle
import dyds.crypto.cecoin.news.domain.usecase.FakeGetCryptoNewsUseCase
import dyds.crypto.cecoin.news.domain.usecase.GetCryptoNewsUseCase
import dyds.crypto.cecoin.core.domain.state.Fallible
import dyds.crypto.cecoin.core.domain.state.Loadable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class NewsViewModelTest {

    @Test
    fun `initial state is Loading before loadNews is called`() = runTest {
        val viewModel = createViewModel()
        assertEquals(Loadable.Loading, viewModel.asyncNews.first())
    }

    @Test
    fun `loadNews emits success`() = runTest {
        val expected = listOf(
            NewsArticle("Title", "Desc", "url", null, "Source", "2024-01-01"),
        )
        val fake = FakeGetCryptoNewsUseCase(articles = expected)
        val viewModel = createViewModel(fake)

        viewModel.loadNews()
        val result = viewModel.asyncNews.first { it !is Loadable.Loading }

        val loaded = assertIs<Loadable.Loaded<Fallible<List<NewsArticle>>>>(result)
        val success = assertIs<Fallible.Success<List<NewsArticle>>>(loaded.value)
        assertEquals(expected, success.value)
    }

    @Test
    fun `loadNews emits failed when use case fails`() = runTest {
        val fake = FakeGetCryptoNewsUseCase(exception = RuntimeException("API error"))
        val viewModel = createViewModel(fake)

        viewModel.loadNews()
        val result = viewModel.asyncNews.first { it !is Loadable.Loading }

        assertIs<Loadable.Loaded<*>>(result)
        val fallible = (result as Loadable.Loaded).value
        assertIs<Fallible.Failed>(fallible)
    }

    @Test
    fun `onSearchQueryChange updates search query in uiState`() = runTest {
        val fake = FakeGetCryptoNewsUseCase()
        val viewModel = createViewModel(fake)

        viewModel.onSearchQueryChange("BTC")

        val state = viewModel.uiState.first()
        assertEquals("BTC", state.searchQuery)
    }

    @Test
    fun `retryLoadNews reloads after failure`() = runTest {
        val fake = FakeGetCryptoNewsUseCase(exception = RuntimeException("fail"))
        val viewModel = createViewModel(fake)

        viewModel.loadNews()
        viewModel.asyncNews.first { it !is Loadable.Loading }

        fake.exception = null
        fake.articles = listOf(
            NewsArticle("Title", "Desc", "url", null, "Source", "2024-01-01"),
        )

        viewModel.retryLoadNews()

        val result = viewModel.asyncNews.first { it is Loadable.Loaded && it.value is Fallible.Success }
        val loaded = assertIs<Loadable.Loaded<Fallible<List<NewsArticle>>>>(result)
        val success = assertIs<Fallible.Success<List<NewsArticle>>>(loaded.value)
        assertEquals(1, success.value.size)
    }

    @Test
    fun `onCancelLoadNews emits Cancelled when load is in progress`() = runTest {
        val loadStarted = MutableStateFlow(false)
        val useCase = object : GetCryptoNewsUseCase {
            override suspend fun invoke(): Fallible<List<NewsArticle>> {
                loadStarted.value = true
                awaitCancellation()
            }
        }
        val viewModel = NewsViewModel(useCase)

        viewModel.loadNews()
        loadStarted.first { it }

        viewModel.onCancelLoadNews()

        val cancelled = viewModel.asyncNews.first { it is Loadable.Cancelled }
        assertIs<Loadable.Cancelled>(cancelled)
    }

    @Test
    fun `onCancelLoadNews does nothing when no active job`() = runTest {
        val fake = FakeGetCryptoNewsUseCase()
        val viewModel = createViewModel(fake)

        viewModel.loadNews()
        viewModel.asyncNews.first { it !is Loadable.Loading }
        viewModel.onCancelLoadNews()
        viewModel.onCancelLoadNews()

        val state = viewModel.uiState.first()
        assertEquals("", state.searchQuery)
    }

    @Test
    fun `onCancelLoadNews after load completed does not crash`() = runTest {
        val fake = FakeGetCryptoNewsUseCase()
        val viewModel = createViewModel(fake)

        viewModel.loadNews()
        viewModel.asyncNews.first { it !is Loadable.Loading }
        viewModel.onCancelLoadNews()
    }

    private fun createViewModel(fake: FakeGetCryptoNewsUseCase = FakeGetCryptoNewsUseCase()): NewsViewModel {
        return NewsViewModel(fake)
    }
}
