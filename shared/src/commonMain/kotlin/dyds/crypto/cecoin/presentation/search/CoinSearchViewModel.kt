package dyds.crypto.cecoin.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dyds.crypto.cecoin.domain.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveFavoritesUseCase
import dyds.crypto.cecoin.domain.usecase.ToggleFavoriteUseCase
import dyds.crypto.cecoin.presentation.utils.AsyncResult
import dyds.crypto.cecoin.utils.AppError
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable
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
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CoinSearchUiState())
    val uiState: StateFlow<CoinSearchUiState> = _uiState.asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, CoinSearchUiState())

    private val _asyncAvailableSymbols = MutableStateFlow<AsyncResult<List<String>>>(Loadable.Loading)

    val favorites: StateFlow<Set<String>> = observeFavoritesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptySet())

    val filteredCoins = combine(_uiState, _asyncAvailableSymbols, favorites) { uiState, asyncSymbols, favs ->
        when (asyncSymbols) {
            is Loadable.Loading -> Loadable.Loading
            is Loadable.Loaded -> {
                when (val fallibleSymbols = asyncSymbols.value) {
                    is Fallible.Failed -> Loadable.Loaded(fallibleSymbols)
                    is Fallible.Success -> {
                        val symbols = fallibleSymbols.value
                        val filtered = when (uiState.filterMode) {
                            FilterMode.ALL -> symbols
                            FilterMode.FAVORITES -> symbols.filter { it in favs }
                        }
                        val result = if (uiState.searchQuery.isEmpty()) {
                            filtered
                        } else {
                            filtered.filter { coin ->
                                coin.contains(uiState.searchQuery, ignoreCase = true)
                            }
                        }
                        Loadable.Loaded(Fallible.Success(result))
                    }
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
            } catch (e: Exception) {
                _asyncAvailableSymbols.value = Loadable.Loaded(
                    Fallible.Failed(AppError.GenericError(e, "Failed to load symbols"))
                )
            }
        }
    }

    fun retryLoadSymbols() {
        loadSymbols()
    }

    fun onCancelLoadSymbols() {
        loadSymbolsJob?.cancel()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectCoin(coin: String) {
        _uiState.value = _uiState.value.copy(selectedCoin = coin)
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