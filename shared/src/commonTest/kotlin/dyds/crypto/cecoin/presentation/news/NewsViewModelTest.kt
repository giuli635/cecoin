package dyds.crypto.cecoin.presentation.news

import dyds.crypto.cecoin.domain.model.NewsArticle
import dyds.crypto.cecoin.domain.usecase.FakeGetCryptoNewsUseCase
import dyds.crypto.cecoin.domain.usecase.GetCryptoNewsUseCase
import dyds.crypto.cecoin.utils.AppError
import kotlinx.coroutines.awaitCancellation
import dyds.crypto.cecoin.utils.ErrorClassifier
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class NewsViewModelTest {

    @Test
    fun `init loads news and emits success`() = runTest {
        val expected = listOf(
            NewsArticle("Title", "Desc", "url", null, "Source", "2024-01-01"),
        )
        val fake = FakeGetCryptoNewsUseCase(articles = expected)
        val viewModel = createViewModel(fake)

        val result = viewModel.asyncNews.first { it !is Loadable.Loading }

        assertIs<Loadable.Loaded<*>>(result)
        val fallible = (result as Loadable.Loaded).value
        assertIs<Fallible.Success<*>>(fallible)
        @Suppress("UNCHECKED_CAST")
        assertEquals(expected, (fallible as Fallible.Success<*>).value)
    }

    @Test
    fun `loadNews emits failed when use case throws`() = runTest {
        val fake = FakeGetCryptoNewsUseCase(exception = RuntimeException("API error"))
        val viewModel = createViewModel(fake)

        val result = viewModel.asyncNews.first { it !is Loadable.Loading }

        assertIs<Loadable.Loaded<*>>(result)
        val fallible = (result as Loadable.Loaded).value
        assertIs<Fallible.Failed>(fallible)
        val error = (fallible as Fallible.Failed).error
        assertIs<AppError.GenericError>(error)
        assertTrue(error.userMessage.contains("Error al cargar noticias"))
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

        viewModel.asyncNews.first { it !is Loadable.Loading }

        fake.exception = null
        fake.articles = listOf(
            NewsArticle("Title", "Desc", "url", null, "Source", "2024-01-01"),
        )

        viewModel.retryLoadNews()

        val result = viewModel.asyncNews.first { it is Loadable.Loaded && it.value is Fallible.Success }
        @Suppress("UNCHECKED_CAST")
        val articles = ((result as Loadable.Loaded).value as Fallible.Success<*>).value as List<*>
        assertEquals(1, articles.size)
    }

    @Test
    fun `onCancelLoadNews cancels active job`() = runTest {
        val fake = FakeGetCryptoNewsUseCase()
        val viewModel = createViewModel(fake)

        viewModel.asyncNews.first { it !is Loadable.Loading }
        viewModel.onCancelLoadNews()

        val state = viewModel.uiState.first()
        assertEquals("", state.searchQuery)
    }

    @Test
    fun `onCancelLoadNews emits Cancelled when load is in progress`() = runTest {
        val loadStarted = MutableStateFlow(false)
        val useCase = object : GetCryptoNewsUseCase {
            override suspend fun invoke(): List<NewsArticle> {
                loadStarted.value = true
                awaitCancellation()
            }
        }
        val viewModel = NewsViewModel(useCase, object : ErrorClassifier() {
            override fun isNetworkError(e: Throwable) = false
        })

        loadStarted.first { it }

        viewModel.onCancelLoadNews()

        val cancelled = viewModel.asyncNews.first { it is Loadable.Cancelled }
        assertIs<Loadable.Cancelled>(cancelled)
    }

    @Test
    fun `onCancelLoadNews does nothing when no active job`() = runTest {
        val fake = FakeGetCryptoNewsUseCase()
        val viewModel = createViewModel(fake)

        viewModel.asyncNews.first { it !is Loadable.Loading }
        viewModel.onCancelLoadNews()
        viewModel.onCancelLoadNews()

        val state = viewModel.uiState.first()
        assertEquals("", state.searchQuery)
    }

    private fun createViewModel(fake: FakeGetCryptoNewsUseCase = FakeGetCryptoNewsUseCase()): NewsViewModel {
        return NewsViewModel(fake, object : ErrorClassifier() {
            override fun isNetworkError(e: Throwable) = false
        })
    }
}
