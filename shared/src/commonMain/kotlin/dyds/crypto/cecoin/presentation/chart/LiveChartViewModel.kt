package dyds.crypto.cecoin.presentation.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.domain.model.toPricePoints
import dyds.crypto.cecoin.domain.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.presentation.chart.model.Granularity
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

class LiveChartViewModel(
    private val getHistoricalPricesUseCase: GetHistoricalPricesUseCase,
    private val observeTradePricesUseCase: ObserveTradePricesUseCase,
    val symbol: String,
    private val historicalPointLimit: Int = 200,
) : ViewModel() {
    val modelProducer = CartesianChartModelProducer()

    private val _asyncLoadState = MutableStateFlow<AsyncResult<Unit>>(Loadable.Loading)
    val asyncLoadState: StateFlow<AsyncResult<Unit>> = _asyncLoadState.asStateFlow()

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
                    getHistoricalPricesUseCase(
                        symbol = symbol,
                        interval = g.interval,
                        limit = historicalPointLimit,
                    )
                }.onFailure {
                    _asyncLoadState.value = Loadable.Loaded(
                        Fallible.Failed(AppError.GenericError(it, "Failed to load chart"))
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

            observeTradePricesUseCase(symbol)
                .retryWhen { cause, _ ->
                    if (cause is CancellationException) return@retryWhen false
                    delay(1_000.milliseconds)
                    true
                }
                .sample(33.milliseconds)
                .collect { trade ->
                    points.foldTradePrice(trade, g)
                    pushModel()
                }
        }
    }

    private suspend fun pushModel() {
        if (points.isEmpty()) return
        val last = points.last()
        _lastPrice.value = last.price
        val x = points.map { it.timestamp.toDouble() }
        val y = points.map { it.price }
        val lastX = last.timestamp.toDouble()
        modelProducer.runTransaction {
            lineModel {
                series(x = x, y = y)
                series(x = listOf(x.first(), lastX), y = listOf(last.price, last.price))
                series(x = listOf(lastX), y = listOf(last.price))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        priceJob?.cancel()
    }
}