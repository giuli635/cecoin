package dyds.crypto.cecoin.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dyds.crypto.cecoin.domain.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.presentation.utils.AsyncResult
import dyds.crypto.cecoin.utils.AppError
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val _asyncAvailableSymbols = MutableStateFlow<AsyncResult<List<String>>>(Loadable.Loading)

    val filteredCoins = combine(_uiState, _asyncAvailableSymbols) { uiState, asyncSymbols ->
        when (asyncSymbols) {
            is Loadable.Loading -> Loadable.Loading
            is Loadable.Loaded -> {
                when (val fallibleSymbols = asyncSymbols.value) {
                    is Fallible.Failed -> Loadable.Loaded(fallibleSymbols)
                    is Fallible.Success -> {
                        val symbols = fallibleSymbols.value
                        if (uiState.searchQuery.isEmpty()) {
                            Loadable.Loaded(Fallible.Success(symbols))
                        } else {
                            Loadable.Loaded(Fallible.Success(symbols.filter { coin ->
                                coin.contains(uiState.searchQuery, ignoreCase = true)
                            }))
                        }
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
}