package dyds.crypto.cecoin.presentation.chart

import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.presentation.chart.util.PriceAccumulator
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

    override fun invoke(symbol: String): Flow<TradePrice> {
        if (exception != null) return flow { throw exception }
        return channelFlow {
            for (trade in _channel) {
                send(trade)
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
