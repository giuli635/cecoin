package dyds.crypto.cecoin.chart.data.datasource

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import kotlinx.coroutines.flow.Flow

interface CoinPriceDataSource {
    fun observePrices(symbol: CryptoSymbol): Flow<PricePoint>
}

