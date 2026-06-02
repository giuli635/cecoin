package dyds.crypto.cecoin.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow

data class CoinSearchUiState(
    val searchQuery: String = "",
    val selectedCoin: String? = null,
)

class CoinSearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CoinSearchUiState())
    val uiState = _uiState.asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, CoinSearchUiState())

    // Common trading pairs on Binance
    private val commonCoins = listOf(
        "BTCUSDT",
        "ETHUSDT",
        "BNBUSDT",
        "SOLUSDT",
        "XRPUSDT",
        "ADAUSDT",
        "DOGEUSDT",
        "MATICUSDT",
        "UNIUSDT",
        "LTCUSDT",
        "AVAXUSDT",
        "LINKUSDT",
        "NEARUSDT",
        "ATOMUSDT",
        "ARKUSDT",
        "ETHUSD",
        "BTCBUSD",
        "ETHBUSD",
        "BNBBUSD",
    ).distinct()

    val filteredCoins = _uiState
        .map { state ->
            if (state.searchQuery.isEmpty()) {
                commonCoins
            } else {
                commonCoins.filter { coin ->
                    coin.contains(state.searchQuery, ignoreCase = true)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectCoin(coin: String) {
        _uiState.value = _uiState.value.copy(selectedCoin = coin)
    }
}


