package dyds.crypto.cecoin.data.chart.datasource

import dyds.crypto.cecoin.domain.chart.model.TradePrice
import kotlinx.coroutines.flow.Flow

interface CoinPriceDataSource {
    fun tradePrices(symbol: String): Flow<TradePrice>
}

