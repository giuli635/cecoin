package dyds.crypto.cecoin.chart.data.datasource

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import kotlinx.coroutines.flow.Flow

interface CoinPriceDataSource {
    fun observePrices(symbol: String): Flow<PricePoint>
}

