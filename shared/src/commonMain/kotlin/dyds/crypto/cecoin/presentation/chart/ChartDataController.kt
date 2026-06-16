package dyds.crypto.cecoin.presentation.chart

import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.presentation.chart.model.ChartData
import dyds.crypto.cecoin.presentation.chart.model.Granularity
import dyds.crypto.cecoin.presentation.chart.util.foldTradePrice
import dyds.crypto.cecoin.utils.AppError
import dyds.crypto.cecoin.utils.ErrorClassifier
import dyds.crypto.cecoin.utils.Fallible
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val RETRY_DELAY_MS = 1_000L
private const val MAX_STREAM_RETRIES = 3
private const val STREAM_FAILED = "La transmisión en vivo falló"

class ChartDataController(
    private val observeTradePricesUseCase: ObserveTradePricesUseCase,
    private var granularity: Granularity,
    private val scope: CoroutineScope,
    val symbol: String,
    historical: List<PricePoint>,
    private val errorClassifier: ErrorClassifier,
    private val retryDelayMs: Long = RETRY_DELAY_MS,
    private val maxRetries: Int = MAX_STREAM_RETRIES,
) {
    private val _chartData = MutableStateFlow<ChartData>(
        Fallible.Success(emptyList())
    )
    val chartData: StateFlow<ChartData> = _chartData.asStateFlow()

    private val points = mutableListOf<PricePoint>()
    private var streamJob: Job? = null

    init {
        points.clear()
        points.addAll(historical)
        _chartData.value = Fallible.Success(points.toList())
    }

    fun startStream() {
        streamJob?.cancel()
        streamJob = scope.launch {
            var retryCount = 0
            observeTradePricesUseCase(symbol)
                .retryWhen { cause, _ ->
                    if (cause is CancellationException) return@retryWhen false
                    retryCount++
                    _chartData.value = Fallible.Failed(errorClassifier.classify(cause, STREAM_FAILED))
                    if (retryCount >= maxRetries) return@retryWhen false
                    delay(retryDelayMs.milliseconds)
                    true
                }
                .catch { e ->
                    if (e is CancellationException) throw e
                    _chartData.value = Fallible.Failed(errorClassifier.classify(e, STREAM_FAILED))
                }
                .collect { trade ->
                    points.foldTradePrice(trade, granularity)
                    _chartData.value = Fallible.Success(points.toList())
                }
        }
    }

    fun cancel() {
        streamJob?.cancel()
    }
}
