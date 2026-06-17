package dyds.crypto.cecoin.chart.domain.usecase

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.domain.repository.TradePriceRepository
import dyds.crypto.cecoin.core.utils.ErrorStrings
import dyds.crypto.cecoin.core.utils.error.ErrorClassifier
import dyds.crypto.cecoin.core.utils.state.Fallible
import dyds.crypto.cecoin.core.utils.state.runCatchingCancellable
import dyds.crypto.cecoin.core.utils.state.toFallible

interface GetHistoricalPricesUseCase {
    suspend operator fun invoke(symbol: String, interval: String = "1m", limit: Int = 200): Fallible<List<PricePoint>>
}

class GetHistoricalPricesUseCaseImpl(
    private val repository: TradePriceRepository,
    private val errorClassifier: ErrorClassifier,
) : GetHistoricalPricesUseCase {
    override suspend operator fun invoke(symbol: String, interval: String, limit: Int): Fallible<List<PricePoint>> {
        return runCatchingCancellable { repository.getHistoricalPrices(symbol, interval, limit) }
            .toFallible(errorClassifier, ErrorStrings.HISTORICAL_DATA)
    }
}
