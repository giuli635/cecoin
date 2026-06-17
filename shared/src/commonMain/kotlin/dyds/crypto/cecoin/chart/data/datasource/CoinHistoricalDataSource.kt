package dyds.crypto.cecoin.chart.data.datasource

import dyds.crypto.cecoin.chart.domain.model.TradePrice

interface CoinHistoricalDataSource {
    suspend fun getHistoricalPrices(symbol: String, interval: String = "1m", limit: Int = 200): List<TradePrice>
}
