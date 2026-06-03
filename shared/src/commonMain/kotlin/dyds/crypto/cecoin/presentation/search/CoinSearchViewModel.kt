package dyds.crypto.cecoin.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dyds.crypto.cecoin.domain.usecase.GetAvailableSymbolsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SymbolLoadingState {
    data object Loading : SymbolLoadingState()
    data object Loaded : SymbolLoadingState()
    data class Error(val message: String) : SymbolLoadingState()
}

data class CoinSearchUiState(
    val searchQuery: String = "",
    val selectedCoin: String? = null,
)

class CoinSearchViewModel(
    private val getAvailableSymbolsUseCase: GetAvailableSymbolsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CoinSearchUiState())
    val uiState = _uiState.asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, CoinSearchUiState())

    private val _availableSymbols = MutableStateFlow<List<String>>(emptyList())
    private val _loadingState = MutableStateFlow<SymbolLoadingState>(SymbolLoadingState.Loading)

    val loadingState = _loadingState.asStateFlow()
    val availableSymbols = _availableSymbols.asStateFlow()

    val filteredCoins = combine(_uiState, _availableSymbols) { state, symbols ->
        if (state.searchQuery.isEmpty()) {
            symbols
        } else {
            symbols.filter { coin ->
                coin.contains(state.searchQuery, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadSymbols()
    }

    fun loadSymbols() {
        viewModelScope.launch {
            _loadingState.value = SymbolLoadingState.Loading
            try {
                val symbols = getAvailableSymbolsUseCase()
                    .map { it.symbol }
                    .sorted()
                _availableSymbols.value = symbols
                _loadingState.value = SymbolLoadingState.Loaded
            } catch (e: Exception) {
                _loadingState.value = SymbolLoadingState.Error(
                    e.message ?: "Failed to load symbols"
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectCoin(coin: String) {
        _uiState.value = _uiState.value.copy(selectedCoin = coin)
    }
}


