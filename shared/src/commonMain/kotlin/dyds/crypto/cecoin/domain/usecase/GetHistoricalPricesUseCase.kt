package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.repository.CecoinRepository

class GetHistoricalPricesUseCase(
    private val repository: CecoinRepository,
) {
    suspend operator fun invoke(symbol: String, interval: String = "1m", limit: Int = 200): List<TradePrice> =
        repository.getHistoricalPrices(symbol, interval, limit)
}
