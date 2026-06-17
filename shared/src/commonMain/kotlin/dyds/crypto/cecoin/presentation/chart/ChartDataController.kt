package dyds.crypto.cecoin.presentation.chart

import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.presentation.chart.model.ChartData
import dyds.crypto.cecoin.presentation.chart.util.PriceAccumulator
import dyds.crypto.cecoin.utils.Fallible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChartDataController(
    private val observeTradePricesUseCase: ObserveTradePricesUseCase,
    private val priceAccumulator: PriceAccumulator,
    private val scope: CoroutineScope,
    val symbol: String,
) {
    private val _chartData = MutableStateFlow<ChartData>(
        Fallible.Success(emptyList())
    )
    val chartData: StateFlow<ChartData> = _chartData.asStateFlow()

    private var streamJob: Job? = null

    init {
        _chartData.value = Fallible.Success(priceAccumulator.snapshot())
    }

    fun startStream() {
        streamJob?.cancel()
        streamJob = scope.launch {
            observeTradePricesUseCase(symbol).collect { fallible ->
                _chartData.value = fallible.map { trade ->
                    priceAccumulator.accumulate(trade)
                    priceAccumulator.snapshot()
                }
            }
        }
    }

    fun cancel() {
        streamJob?.cancel()
    }
}
