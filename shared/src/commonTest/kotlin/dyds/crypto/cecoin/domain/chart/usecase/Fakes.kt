package dyds.crypto.cecoin.domain.chart.usecase

import dyds.crypto.cecoin.domain.chart.model.TradePrice
import dyds.crypto.cecoin.domain.chart.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.utils.state.Fallible
import dyds.crypto.cecoin.utils.error.AppError

class FakeGetHistoricalPricesUseCase(
    var prices: List<TradePrice> = emptyList(),
    var exception: Throwable? = null,
) : GetHistoricalPricesUseCase {
    var lastSymbol: String = ""
    var lastInterval: String = ""
    var lastLimit: Int = 0

    override suspend fun invoke(symbol: String, interval: String, limit: Int): Fallible<List<TradePrice>> {
        lastSymbol = symbol
        lastInterval = interval
        lastLimit = limit
        if (exception != null) return Fallible.Failed(AppError.GenericError(exception!!, "fallo"))
        return Fallible.Success(prices)
    }
}
