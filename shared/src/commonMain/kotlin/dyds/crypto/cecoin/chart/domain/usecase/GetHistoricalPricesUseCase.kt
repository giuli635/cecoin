package dyds.crypto.cecoin.chart.domain.usecase

import cecoin.shared.generated.resources.*
import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.domain.repository.PriceRepository
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.domain.error.ErrorClassifier
import dyds.crypto.cecoin.core.domain.state.Fallible
import dyds.crypto.cecoin.core.domain.state.runCatchingCancellable
import dyds.crypto.cecoin.core.domain.state.toFallible
import org.jetbrains.compose.resources.getString

interface GetHistoricalPricesUseCase {
    suspend operator fun invoke(symbol: CryptoSymbol, interval: String = "1m", limit: Int = 200): Fallible<List<PricePoint>>
}

class GetHistoricalPricesUseCaseImpl(
    private val repository: PriceRepository,
    private val errorClassifier: ErrorClassifier,
) : GetHistoricalPricesUseCase {
    override suspend operator fun invoke(symbol: CryptoSymbol, interval: String, limit: Int): Fallible<List<PricePoint>> {
        return runCatchingCancellable { repository.getHistoricalPrices(symbol, interval, limit) }
            .toFallible(errorClassifier, getString(Res.string.error_historical_data))
    }
}
