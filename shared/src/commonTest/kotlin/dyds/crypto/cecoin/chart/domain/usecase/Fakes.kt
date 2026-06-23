package dyds.crypto.cecoin.chart.domain.usecase

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.domain.state.Fallible
import dyds.crypto.cecoin.core.domain.error.AppError

class FakeGetHistoricalPricesUseCase(
    var prices: List<PricePoint> = emptyList(),
    var exception: Throwable? = null,
) : GetHistoricalPricesUseCase {
    var lastSymbol: CryptoSymbol? = null
    var lastInterval: String = ""
    var lastLimit: Int = 0

    override suspend fun invoke(symbol: CryptoSymbol, interval: String, limit: Int): Fallible<List<PricePoint>> {
        lastSymbol = symbol
        lastInterval = interval
        lastLimit = limit
        if (exception != null) return Fallible.Failed(AppError.GenericError(exception!!, "fallo"))
        return Fallible.Success(prices)
    }
}
