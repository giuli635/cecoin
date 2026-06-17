package dyds.crypto.cecoin.data.chart.datasource

import dyds.crypto.cecoin.domain.chart.model.TradePrice

interface CoinHistoricalDataSource {
    suspend fun getHistoricalPrices(symbol: String, interval: String = "1m", limit: Int = 200): List<TradePrice>
}
