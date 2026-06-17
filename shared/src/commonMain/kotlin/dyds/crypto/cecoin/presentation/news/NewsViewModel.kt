package dyds.crypto.cecoin.presentation.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dyds.crypto.cecoin.domain.news.model.NewsArticle
import dyds.crypto.cecoin.domain.news.usecase.GetCryptoNewsUseCase
import dyds.crypto.cecoin.presentation.utils.AsyncResult
import dyds.crypto.cecoin.presentation.utils.launchAsync
import dyds.crypto.cecoin.utils.state.Loadable
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

    init {
        loadNews()
    }

    fun loadNews() {
        loadNewsJob?.cancel()
        loadNewsJob = launchAsync(_asyncNews) {
            getCryptoNewsUseCase()
        }
    }

    fun retryLoadNews() {
        loadNews()
    }

    fun onCancelLoadNews() {
        loadNewsJob?.cancel()
        loadNewsJob = null
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}
