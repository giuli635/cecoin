package dyds.crypto.cecoin.chart.presentation

import dyds.crypto.cecoin.chart.domain.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.chart.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.chart.presentation.model.ChartData
import dyds.crypto.cecoin.chart.presentation.model.Granularity
import dyds.crypto.cecoin.chart.presentation.util.PriceAccumulatorImpl
import dyds.crypto.cecoin.core.utils.state.Fallible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChartDataController(
    private val getHistoricalPrices: GetHistoricalPricesUseCase,
    private val observeTradePrices: ObserveTradePricesUseCase,
    private val scope: CoroutineScope,
) {
    private val _chartData = MutableStateFlow<ChartData>(Fallible.Success(emptyList()))
    val chartData: StateFlow<ChartData> = _chartData.asStateFlow()

    private var streamJob: Job? = null

    suspend fun observe(symbol: String, g: Granularity, limit: Int): Fallible<Flow<ChartData>> {
        cancel()
        return getHistoricalPrices(symbol, g.interval, limit)
            .map { historical ->
                val accumulator = PriceAccumulatorImpl(g, historical)
                _chartData.value = Fallible.Success(accumulator.snapshot())
                startStream(accumulator, symbol)
                _chartData.asStateFlow()
            }
    }

    private fun startStream(accumulator: PriceAccumulatorImpl, symbol: String) {
        streamJob = scope.launch {
            observeTradePrices(symbol).collect { fallible ->
                _chartData.value = fallible.map { trade ->
                    accumulator.accumulateAndSnapshot(trade)
                }
            }
        }
    }

    fun cancel() {
        streamJob?.cancel()
    }
}
