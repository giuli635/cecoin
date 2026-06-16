package dyds.crypto.cecoin.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dyds.crypto.cecoin.domain.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveFavoritesUseCase
import dyds.crypto.cecoin.domain.usecase.ToggleFavoriteUseCase
import dyds.crypto.cecoin.presentation.search.util.filterBy
import dyds.crypto.cecoin.presentation.utils.AsyncResult
import dyds.crypto.cecoin.utils.ErrorClassifier
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val FAILED_TO_LOAD_SYMBOLS = "Error al cargar símbolos"

class CoinSearchViewModel(
    private val getAvailableSymbolsUseCase: GetAvailableSymbolsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val errorClassifier: ErrorClassifier,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CoinSearchUiState())
    val uiState: StateFlow<CoinSearchUiState> = _uiState.asStateFlow()

    private val _asyncAvailableSymbols = MutableStateFlow<AsyncResult<List<String>>>(Loadable.Loading)

    val favorites: StateFlow<Set<String>> = observeFavoritesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptySet())

    val filteredCoins = combine(_uiState, _asyncAvailableSymbols, favorites) { uiState, asyncSymbols, favs ->
        when (asyncSymbols) {
            is Loadable.Loading -> Loadable.Loading
            is Loadable.Cancelled -> Loadable.Cancelled
            is Loadable.Loaded -> {
                when (val fallibleSymbols = asyncSymbols.value) {
                    is Fallible.Failed -> Loadable.Loaded(fallibleSymbols)
                    is Fallible.Success ->
                        Loadable.Loaded(Fallible.Success(
                            fallibleSymbols.value.filterBy(uiState.searchQuery, uiState.filterMode, favs)
                        ))
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, Loadable.Loading)

    private var loadSymbolsJob: Job? = null

    init {
        loadSymbols()
    }

    fun loadSymbols() {
        loadSymbolsJob?.cancel()
        loadSymbolsJob = viewModelScope.launch {
            _asyncAvailableSymbols.value = Loadable.Loading
            try {
                val symbols = getAvailableSymbolsUseCase()
                    .map { it.symbol }
                    .sorted()
                _asyncAvailableSymbols.value = Loadable.Loaded(Fallible.Success(symbols))
            } catch (_: CancellationException) {
                _asyncAvailableSymbols.value = Loadable.Cancelled
            } catch (e: Exception) {
                _asyncAvailableSymbols.value = Loadable.Loaded(
                    Fallible.Failed(errorClassifier.classify(e, FAILED_TO_LOAD_SYMBOLS))
                )
            }
        }
    }

    fun retryLoadSymbols() {
        loadSymbols()
    }

    fun onCancelLoadSymbols() {
        loadSymbolsJob?.cancel()
        loadSymbolsJob = null
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setFilterMode(mode: FilterMode) {
        _uiState.value = _uiState.value.copy(filterMode = mode)
    }

    fun toggleFavorite(symbol: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(symbol)
        }
    }
}