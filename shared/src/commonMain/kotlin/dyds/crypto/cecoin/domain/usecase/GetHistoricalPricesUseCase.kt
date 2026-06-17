package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.repository.TradePriceRepository
import dyds.crypto.cecoin.utils.ErrorClassifier
import dyds.crypto.cecoin.utils.Fallible
import kotlinx.coroutines.CancellationException

interface GetHistoricalPricesUseCase {
    suspend operator fun invoke(symbol: String, interval: String = "1m", limit: Int = 200): Fallible<List<TradePrice>>
}

class GetHistoricalPricesUseCaseImpl(
    private val repository: TradePriceRepository,
    private val errorClassifier: ErrorClassifier,
) : GetHistoricalPricesUseCase {
    override suspend operator fun invoke(symbol: String, interval: String, limit: Int): Fallible<List<TradePrice>> {
        return try {
            Fallible.Success(repository.getHistoricalPrices(symbol, interval, limit))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Fallible.Failed(errorClassifier.classify(e, "Error al cargar datos históricos"))
        }
    }
}
