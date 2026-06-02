package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.repository.CecoinRepository
import kotlinx.coroutines.flow.Flow

class ObserveTradePricesUseCase(
    private val repository: CecoinRepository,
) {
    operator fun invoke(symbol: String): Flow<TradePrice> = repository.observeTradePrices(symbol)
}


