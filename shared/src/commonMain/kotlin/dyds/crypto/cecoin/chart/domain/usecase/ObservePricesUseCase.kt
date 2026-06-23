package dyds.crypto.cecoin.chart.domain.usecase

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.domain.repository.PriceRepository
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.domain.error.ErrorClassifier
import dyds.crypto.cecoin.core.domain.state.Fallible
import dyds.crypto.cecoin.core.domain.state.runCatchingCancellable
import dyds.crypto.cecoin.core.domain.state.toFallible
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.milliseconds

interface ObservePricesUseCase {
    operator fun invoke(symbol: CryptoSymbol): Flow<Fallible<PricePoint>>
}

class ObservePricesUseCaseImpl(
    private val repository: PriceRepository,
    private val errorClassifier: ErrorClassifier,
    private val retryDelayMs: Long = 1_000L,
    private val maxRetries: Int = 3,
    private val contextKey: String,
) : ObservePricesUseCase {
    override fun invoke(symbol: CryptoSymbol): Flow<Fallible<PricePoint>> = flow {
        var retryCount = 0
        while (retryCount <= maxRetries) {
            runCatchingCancellable {
                repository.observePrices(symbol).collect { point ->
                    emit(Fallible.Success(point))
                }
            }.toFallible(errorClassifier, contextKey)
                .onFailure {
                    emit(Fallible.Failed(it))
                    if (++retryCount <= maxRetries) delay(retryDelayMs.milliseconds)
                }
        }
    }
}
