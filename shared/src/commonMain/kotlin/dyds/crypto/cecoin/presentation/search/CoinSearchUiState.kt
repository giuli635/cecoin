package dyds.crypto.cecoin.presentation.search

enum class FilterMode { ALL, FAVORITES }

data class CoinSearchUiState(
    val searchQuery: String = "",
    val selectedCoin: String? = null,
    val filterMode: FilterMode = FilterMode.ALL,
)
