package dyds.crypto.cecoin.presentation.viewmodel

import dyds.crypto.cecoin.domain.FakeNewsRepository
import dyds.crypto.cecoin.domain.model.NewsArticle
import dyds.crypto.cecoin.domain.usecase.GetCryptoNewsUseCase
import dyds.crypto.cecoin.presentation.news.NewsViewModel
import dyds.crypto.cecoin.utils.AppError
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable
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
        val repo = FakeNewsRepository(articles = expected)
        val viewModel = createViewModel(repo)

        val result = viewModel.asyncNews.first { it !is Loadable.Loading }

        assertIs<Loadable.Loaded<*>>(result)
        val fallible = (result as Loadable.Loaded).value
        assertIs<Fallible.Success<*>>(fallible)
        @Suppress("UNCHECKED_CAST")
        assertEquals(expected, (fallible as Fallible.Success<*>).value)
    }

    @Test
    fun `loadNews emits failed when use case throws`() = runTest {
        val repo = FakeNewsRepository(exception = RuntimeException("API error"))
        val viewModel = createViewModel(repo)

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
        val repo = FakeNewsRepository()
        val viewModel = createViewModel(repo)

        viewModel.onSearchQueryChange("BTC")

        val state = viewModel.uiState.first()
        assertEquals("BTC", state.searchQuery)
    }

    @Test
    fun `retryLoadNews reloads after failure`() = runTest {
        val repo = FakeNewsRepository(exception = RuntimeException("fail"))
        val viewModel = createViewModel(repo)

        viewModel.asyncNews.first { it !is Loadable.Loading }

        repo.exception = null
        repo.articles = listOf(
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
        val repo = FakeNewsRepository()
        val viewModel = createViewModel(repo)

        viewModel.asyncNews.first { it !is Loadable.Loading }
        viewModel.onCancelLoadNews()

        val state = viewModel.uiState.first()
        assertEquals("", state.searchQuery)
    }

    @Test
    fun `onCancelLoadNews does nothing when no active job`() = runTest {
        val repo = FakeNewsRepository()
        val viewModel = createViewModel(repo)

        viewModel.onCancelLoadNews()

        val state = viewModel.uiState.first()
        assertEquals("", state.searchQuery)
    }

    private fun createViewModel(repo: FakeNewsRepository): NewsViewModel {
        val useCase = GetCryptoNewsUseCase(repo)
        return NewsViewModel(useCase)
    }
}
