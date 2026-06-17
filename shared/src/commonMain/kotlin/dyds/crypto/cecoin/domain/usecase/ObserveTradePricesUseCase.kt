package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.repository.TradePriceRepository
import dyds.crypto.cecoin.utils.ErrorClassifier
import dyds.crypto.cecoin.utils.Fallible
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
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
                send(Fallible.Failed(errorClassifier.classify(e, "La transmisión en vivo falló")))
                if (++retryCount > maxRetries) break
                delay(retryDelayMs.milliseconds)
            }
        }
    }
}
