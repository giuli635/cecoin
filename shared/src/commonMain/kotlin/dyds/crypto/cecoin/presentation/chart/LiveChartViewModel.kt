package dyds.crypto.cecoin.presentation.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.domain.model.toPricePoints
import dyds.crypto.cecoin.domain.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.presentation.chart.model.Granularity
import dyds.crypto.cecoin.presentation.chart.util.ChartModelBuilder
import dyds.crypto.cecoin.presentation.chart.util.VicoChartModelBuilder
import dyds.crypto.cecoin.presentation.chart.util.foldTradePrice
import dyds.crypto.cecoin.presentation.utils.AsyncResult
import dyds.crypto.cecoin.utils.AppError
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val DEFAULT_HISTORICAL_LIMIT = 200
private const val RETRY_DELAY_MS = 1_000L
private const val SAMPLE_INTERVAL_MS = 33L
private const val MAX_STREAM_RETRIES = 3
private const val FAILED_TO_LOAD_CHART = "Failed to load chart"

class LiveChartViewModel(
    private val getHistoricalPricesUseCase: GetHistoricalPricesUseCase,
    private val observeTradePricesUseCase: ObserveTradePricesUseCase,
    val symbol: String,
    private val historicalPointLimit: Int = DEFAULT_HISTORICAL_LIMIT,
    private val chartModelBuilder: ChartModelBuilder = VicoChartModelBuilder(),
) : ViewModel() {
    val modelProducer = CartesianChartModelProducer()

    private val _asyncLoadState = MutableStateFlow<AsyncResult<Unit>>(Loadable.Loading)
    val asyncLoadState: StateFlow<AsyncResult<Unit>> = _asyncLoadState.asStateFlow()

    private val _streamState = MutableStateFlow<AsyncResult<Unit>>(Loadable.Loading)
    val streamState: StateFlow<AsyncResult<Unit>> = _streamState.asStateFlow()

    private val _granularity = MutableStateFlow(Granularity.M1)
    val granularity: StateFlow<Granularity> = _granularity.asStateFlow()

    private val _lastPrice = MutableStateFlow(0.0)
    val lastPrice: StateFlow<Double> = _lastPrice.asStateFlow()

    private var priceJob: Job? = null
    private val points = mutableListOf<PricePoint>()
    private var reloadPending = false

    fun setGranularity(g: Granularity) {
        if (_granularity.value == g) return
        _granularity.value = g
        reloadPending = true
        loadPrices()
    }

    @OptIn(FlowPreview::class)
    fun loadPrices() {
        priceJob?.cancel()
        priceJob = viewModelScope.launch {
            if (!reloadPending) {
                _asyncLoadState.value = Loadable.Loading
            }

            val g = _granularity.value

            if (reloadPending) {
                points.clear()
                reloadPending = false
            }

            if (points.isEmpty()) {
                runCatching {
                    getHistoricalPricesUseCase(symbol, g.interval, historicalPointLimit)
                }.onFailure {
                    _asyncLoadState.value = Loadable.Loaded(
                        Fallible.Failed(AppError.GenericError(it, FAILED_TO_LOAD_CHART))
                    )
                    return@launch
                }.onSuccess { historical ->
                    points.addAll(historical.toPricePoints(g.millis))
                    pushModel()
                    _asyncLoadState.value = Loadable.Loaded(Fallible.Success(Unit))
                }
            } else {
                _asyncLoadState.value = Loadable.Loaded(Fallible.Success(Unit))
            }

            var retryCount = 0
            _streamState.value = Loadable.Loading

            observeTradePricesUseCase(symbol)
                .retryWhen { cause, _ ->
                    if (cause is CancellationException) return@retryWhen false
                    retryCount++
                    if (retryCount >= MAX_STREAM_RETRIES) {
                        _streamState.value = Loadable.Loaded(
                            Fallible.Failed(AppError.GenericError(cause, "Live stream failed"))
                        )
                        return@retryWhen false
                    }
                    delay(RETRY_DELAY_MS.milliseconds)
                    true
                }
                .sample(SAMPLE_INTERVAL_MS.milliseconds)
                .collect { trade ->
                    _streamState.value = Loadable.Loaded(Fallible.Success(Unit))
                    points.foldTradePrice(trade, g)
                    pushModel()
                }
        }
    }

    private suspend fun pushModel() {
        if (points.isEmpty()) return
        _lastPrice.value = points.last().price
        chartModelBuilder.buildModel(points, modelProducer)
    }

    override fun onCleared() {
        super.onCleared()
        priceJob?.cancel()
    }
}