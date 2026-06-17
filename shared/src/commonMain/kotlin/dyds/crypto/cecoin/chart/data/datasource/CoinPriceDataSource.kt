package dyds.crypto.cecoin.chart.data.datasource

import dyds.crypto.cecoin.chart.domain.model.TradePrice
import kotlinx.coroutines.flow.Flow

interface CoinPriceDataSource {
    fun tradePrices(symbol: String): Flow<TradePrice>
}

