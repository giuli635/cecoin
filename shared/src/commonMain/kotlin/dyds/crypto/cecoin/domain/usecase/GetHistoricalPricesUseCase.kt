package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.repository.TradePriceRepository

interface GetHistoricalPricesUseCase {
    suspend operator fun invoke(symbol: String, interval: String = "1m", limit: Int = 200): List<TradePrice>
}

class GetHistoricalPricesUseCaseImpl(
    private val repository: TradePriceRepository,
) : GetHistoricalPricesUseCase {
    override suspend operator fun invoke(symbol: String, interval: String, limit: Int): List<TradePrice> =
        repository.getHistoricalPrices(symbol, interval, limit)
}
