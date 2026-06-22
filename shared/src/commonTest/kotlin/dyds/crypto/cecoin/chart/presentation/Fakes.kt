package dyds.crypto.cecoin.chart.presentation

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.domain.usecase.ObservePricesUseCase
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.domain.error.AppError
import dyds.crypto.cecoin.core.domain.state.Fallible
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow

class FakeObservePricesUseCase(
    private val exception: Throwable? = null,
) : ObservePricesUseCase {
    private val _channel = Channel<PricePoint>(Channel.UNLIMITED)
    val emitted: Channel<PricePoint> get() = _channel

    override fun invoke(symbol: CryptoSymbol): Flow<Fallible<PricePoint>> {
        if (exception is CancellationException) return flow { throw exception }
        return channelFlow {
            if (exception != null) {
                send(Fallible.Failed(AppError.GenericError(exception, "La transmisión en vivo falló")))
            } else {
                for (point in _channel) {
                    send(Fallible.Success(point))
                }
            }
        }
    }
}
