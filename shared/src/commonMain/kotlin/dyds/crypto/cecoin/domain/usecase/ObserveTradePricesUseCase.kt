package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.repository.TradePriceRepository
import kotlinx.coroutines.flow.Flow

class ObserveTradePricesUseCase(
    private val repository: TradePriceRepository,
) {
    operator fun invoke(symbol: String): Flow<TradePrice> = repository.observeTradePrices(symbol)
}


