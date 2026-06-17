package dyds.crypto.cecoin.chart.domain.usecase

import dyds.crypto.cecoin.chart.domain.model.TradePrice
import dyds.crypto.cecoin.chart.domain.repository.TradePriceRepository
import dyds.crypto.cecoin.core.utils.ErrorStrings
import dyds.crypto.cecoin.core.utils.error.ErrorClassifier
import dyds.crypto.cecoin.core.utils.state.Fallible
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlin.time.Duration.Companion.milliseconds

interface ObserveTradePricesUseCase {
    operator fun invoke(symbol: String): Flow<Fallible<TradePrice>>
}

class ObserveTradePricesUseCaseImpl(
    private val repository: TradePriceRepository,
    private val errorClassifier: ErrorClassifier,
    private val retryDelayMs: Long = 1_000L,
    private val maxRetries: Int = 3,
) : ObserveTradePricesUseCase {
    override fun invoke(symbol: String): Flow<Fallible<TradePrice>> = channelFlow {
        var retryCount = 0
        while (retryCount <= maxRetries) {
            try {
                repository.observeTradePrices(symbol).collect { trade ->
                    send(Fallible.Success(trade))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                send(Fallible.Failed(errorClassifier.classify(e, ErrorStrings.LIVE_STREAM_FAILED)))
                if (++retryCount > maxRetries) break
                delay(retryDelayMs.milliseconds)
            }
        }
    }
}
