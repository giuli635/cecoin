package dyds.crypto.cecoin.search.presentation

enum class FilterMode { ALL, FAVORITES }

data class CoinSearchUiState(
    val searchQuery: String = "",
    val filterMode: FilterMode = FilterMode.ALL,
)
