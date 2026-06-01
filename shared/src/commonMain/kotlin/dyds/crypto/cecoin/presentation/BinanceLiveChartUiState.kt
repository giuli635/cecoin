package dyds.crypto.cecoin.presentation

enum class ConnectionState {
    Loading, Connected
}

data class BinanceLiveChartUiState(
    val symbol: String = "BTCUSDT",
    val connectionState: ConnectionState = ConnectionState.Loading,
    val prices: List<Double> = emptyList(),
) {
    val lastPrice: Double? get() = prices.lastOrNull()
}

