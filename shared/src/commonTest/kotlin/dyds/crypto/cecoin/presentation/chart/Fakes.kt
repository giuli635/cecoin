package dyds.crypto.cecoin.presentation.chart

import dyds.crypto.cecoin.domain.chart.model.PricePoint
import dyds.crypto.cecoin.domain.chart.model.TradePrice
import dyds.crypto.cecoin.domain.chart.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.presentation.chart.util.PriceAccumulator
import dyds.crypto.cecoin.utils.error.AppError
import dyds.crypto.cecoin.utils.state.Fallible
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow

fun fakeTradePriceFromPricePoint(pricePoint: PricePoint): TradePrice {
    return TradePrice("Fake", pricePoint)
}

fun fakeTradePricesFromPricePoints(vararg pricePoints: PricePoint): List<TradePrice> {
    return pricePoints.map { fakeTradePriceFromPricePoint(it) }
}

class FakeObserveTradePricesUseCase(
    private val exception: Throwable? = null,
) : ObserveTradePricesUseCase {
    private val _channel = Channel<TradePrice>(Channel.UNLIMITED)
    val emitted: Channel<TradePrice> get() = _channel

    override fun invoke(symbol: String): Flow<Fallible<TradePrice>> {
        if (exception is CancellationException) return flow { throw exception }
        return channelFlow {
            if (exception != null) {
                send(Fallible.Failed(AppError.GenericError(exception, "La transmisión en vivo falló")))
            } else {
                for (trade in _channel) {
                    send(Fallible.Success(trade))
                }
            }
        }
    }
}

class FakePriceAccumulator(historical: List<TradePrice> = emptyList()): PriceAccumulator {
    private val points = mutableListOf<PricePoint>()

    init {
        historical.forEach { points.add(it.pricePoint) }
    }

    override fun accumulate(trade: TradePrice) {
        points.add(trade.pricePoint)
    }

    override fun snapshot(): List<PricePoint> {
        return points.toList()
    }
}
