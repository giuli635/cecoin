package dyds.crypto.cecoin.news.presentation

import androidx.lifecycle.ViewModel
import dyds.crypto.cecoin.news.domain.model.NewsArticle
import dyds.crypto.cecoin.news.domain.usecase.GetCryptoNewsUseCase
import dyds.crypto.cecoin.core.presentation.utils.AsyncResult
import dyds.crypto.cecoin.core.presentation.utils.launchLoadable
import dyds.crypto.cecoin.core.domain.state.Loadable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NewsViewModel(
    private val getCryptoNewsUseCase: GetCryptoNewsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private val _asyncNews = MutableStateFlow<AsyncResult<List<NewsArticle>>>(Loadable.Loading)
    val asyncNews: StateFlow<AsyncResult<List<NewsArticle>>> = _asyncNews.asStateFlow()

    private var loadNewsJob: Job? = null

    fun loadNews() {
        loadNewsJob?.cancel()
        loadNewsJob = launchLoadable(_asyncNews) {
            getCryptoNewsUseCase()
        }
    }

    fun retryLoadNews() {
        loadNews()
    }

    fun onCancelLoadNews() {
        loadNewsJob?.cancel()
        _asyncNews.value = Loadable.Cancelled
        loadNewsJob = null
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}
