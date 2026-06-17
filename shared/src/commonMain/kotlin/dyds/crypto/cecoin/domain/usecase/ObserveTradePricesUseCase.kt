package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.repository.TradePriceRepository
import kotlinx.coroutines.flow.Flow

interface ObserveTradePricesUseCase {
    operator fun invoke(symbol: String): Flow<TradePrice>
}

class ObserveTradePricesUseCaseImpl(
    private val repository: TradePriceRepository,
): ObserveTradePricesUseCase {
    override fun invoke(symbol: String): Flow<TradePrice> = repository.observeTradePrices(symbol)
}


