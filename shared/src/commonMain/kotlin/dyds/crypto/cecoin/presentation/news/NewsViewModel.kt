package dyds.crypto.cecoin.presentation.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dyds.crypto.cecoin.domain.model.NewsArticle
import dyds.crypto.cecoin.domain.usecase.GetCryptoNewsUseCase
import dyds.crypto.cecoin.presentation.utils.AsyncResult
import dyds.crypto.cecoin.utils.ErrorClassifier
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val FAILED_TO_LOAD_NEWS = "Error al cargar noticias"

class NewsViewModel(
    private val getCryptoNewsUseCase: GetCryptoNewsUseCase,
    private val errorClassifier: ErrorClassifier,
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
        loadNewsJob = viewModelScope.launch {
            _asyncNews.value = Loadable.Loading
            try {
                val news = getCryptoNewsUseCase()
                _asyncNews.value = Loadable.Loaded(Fallible.Success(news))
            } catch (_: CancellationException) {
                _asyncNews.value = Loadable.Cancelled
            } catch (e: Exception) {
                _asyncNews.value = Loadable.Loaded(
                    Fallible.Failed(errorClassifier.classify(e, FAILED_TO_LOAD_NEWS))
                )
            }
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
