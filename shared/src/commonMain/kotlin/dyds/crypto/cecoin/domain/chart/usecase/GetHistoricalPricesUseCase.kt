package dyds.crypto.cecoin.domain.chart.usecase

import dyds.crypto.cecoin.domain.chart.model.TradePrice
import dyds.crypto.cecoin.domain.chart.repository.TradePriceRepository
import dyds.crypto.cecoin.utils.error.ErrorClassifier
import dyds.crypto.cecoin.utils.state.Fallible
import dyds.crypto.cecoin.utils.state.runCatchingCancellable
import dyds.crypto.cecoin.utils.state.toFallible

interface GetHistoricalPricesUseCase {
    suspend operator fun invoke(symbol: String, interval: String = "1m", limit: Int = 200): Fallible<List<TradePrice>>
}

class GetHistoricalPricesUseCaseImpl(
    private val repository: TradePriceRepository,
    private val errorClassifier: ErrorClassifier,
) : GetHistoricalPricesUseCase {
    override suspend operator fun invoke(symbol: String, interval: String, limit: Int): Fallible<List<TradePrice>> {
        return runCatchingCancellable { repository.getHistoricalPrices(symbol, interval, limit) }
            .toFallible(errorClassifier, "Error al cargar datos históricos")
    }
}
