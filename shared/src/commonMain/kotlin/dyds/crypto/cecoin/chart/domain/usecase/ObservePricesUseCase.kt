package dyds.crypto.cecoin.chart.domain.usecase

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.domain.repository.PriceRepository
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.utils.ErrorStrings
import dyds.crypto.cecoin.core.utils.error.ErrorClassifier
import dyds.crypto.cecoin.core.utils.state.Fallible
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlin.time.Duration.Companion.milliseconds

interface ObservePricesUseCase {
    operator fun invoke(symbol: CryptoSymbol): Flow<Fallible<PricePoint>>
}

class ObservePricesUseCaseImpl(
    private val repository: PriceRepository,
    private val errorClassifier: ErrorClassifier,
    private val retryDelayMs: Long = 1_000L,
    private val maxRetries: Int = 3,
) : ObservePricesUseCase {
    override fun invoke(symbol: CryptoSymbol): Flow<Fallible<PricePoint>> = channelFlow {
        var retryCount = 0
        while (retryCount <= maxRetries) {
            try {
                repository.observePrices(symbol).collect { point ->
                    send(Fallible.Success(point))
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
