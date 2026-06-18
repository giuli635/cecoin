package dyds.crypto.cecoin.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.search.domain.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.search.domain.usecase.ObserveFavoritesUseCase
import dyds.crypto.cecoin.search.domain.usecase.ToggleFavoriteUseCase
import dyds.crypto.cecoin.search.presentation.util.filterBy
import dyds.crypto.cecoin.core.presentation.utils.AsyncResult
import dyds.crypto.cecoin.core.presentation.utils.launchLoadable
import dyds.crypto.cecoin.core.domain.error.AppError
import dyds.crypto.cecoin.core.domain.state.Fallible
import dyds.crypto.cecoin.core.domain.state.Loadable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CoinSearchViewModel(
    private val getAvailableSymbolsUseCase: GetAvailableSymbolsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    observeFavoritesUseCase: ObserveFavoritesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CoinSearchUiState())
    val uiState: StateFlow<CoinSearchUiState> = _uiState.asStateFlow()

    private val _asyncAvailableSymbols = MutableStateFlow<AsyncResult<List<CryptoSymbol>>>(Loadable.Loading)

    private val _toggleError = MutableStateFlow<AppError?>(null)
    val toggleError: StateFlow<AppError?> = _toggleError.asStateFlow()

    val favorites: StateFlow<Set<CryptoSymbol>> = observeFavoritesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptySet())

    val filteredCoins = combine(_uiState, _asyncAvailableSymbols, favorites) { uiState, asyncSymbols, favs ->
        asyncSymbols.map { fallible ->
            fallible.map { symbols ->
                symbols.filterBy(uiState.searchQuery, uiState.filterMode, favs)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, Loadable.Loading)

    private var loadSymbolsJob: Job? = null

    init {
        loadSymbols()
    }

    fun loadSymbols() {
        loadSymbolsJob?.cancel()
        loadSymbolsJob = launchLoadable(_asyncAvailableSymbols) {
            getAvailableSymbolsUseCase()
        }
    }

    fun retryLoadSymbols() {
        loadSymbols()
    }

    fun onCancelLoadSymbols() {
        loadSymbolsJob?.cancel()
        _asyncAvailableSymbols.value = Loadable.Cancelled
        loadSymbolsJob = null
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setFilterMode(mode: FilterMode) {
        _uiState.value = _uiState.value.copy(filterMode = mode)
    }

    fun toggleFavorite(symbol: CryptoSymbol) {
        viewModelScope.launch {
            _toggleError.value = null
            toggleFavoriteUseCase(symbol)
                .onFailure { error -> _toggleError.value = error }
        }
    }
}