package dyds.crypto.cecoin.chart.data.datasource

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol

interface CoinHistoricalDataSource {
    suspend fun getHistoricalPrices(symbol: CryptoSymbol, interval: String = "1m", limit: Int = 200): List<PricePoint>
}
