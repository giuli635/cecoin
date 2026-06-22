package dyds.crypto.cecoin.chart.presentation

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.domain.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.chart.domain.usecase.ObservePricesUseCase
import dyds.crypto.cecoin.chart.presentation.model.ChartData
import dyds.crypto.cecoin.chart.presentation.model.Granularity
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.presentation.utils.AsyncResult
import dyds.crypto.cecoin.core.domain.state.Fallible
import dyds.crypto.cecoin.core.domain.state.Loadable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val DEFAULT_HISTORICAL_LIMIT = 200

class ChartScreenViewModel(
    private val getHistoricalPricesUseCase: GetHistoricalPricesUseCase,
    private val observePricesUseCase: ObservePricesUseCase,
    val symbol: CryptoSymbol,
    private val historicalPointLimit: Int = DEFAULT_HISTORICAL_LIMIT,
) : ViewModel() {
    private val _chartData = MutableStateFlow<ChartData>(Fallible.Success(emptyList()))
    val chartData: StateFlow<ChartData> = _chartData.asStateFlow()

    private val _state = MutableStateFlow<AsyncResult<Flow<ChartData>>>(Loadable.Loading)
    val state: StateFlow<AsyncResult<Flow<ChartData>>> = _state.asStateFlow()

    private var loadJob: Job? = null
    private var accumulatedPoints = mutableListOf<PricePoint>()

    fun cancel() {
        loadJob?.cancel()
        _state.value = Loadable.Cancelled
    }

    fun load(g: Granularity) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = Loadable.Loading
            when (val result = getHistoricalPricesUseCase(symbol, g.interval, historicalPointLimit)) {
                is Fallible.Success -> {
                    accumulatedPoints = result.value.toMutableList()
                    _chartData.value = Fallible.Success(accumulatedPoints.toList())
                    _state.value = Loadable.Loaded(Fallible.Success(_chartData.asStateFlow()))
                    observePricesUseCase(symbol).collect { f ->
                        _chartData.value = f.map { point ->
                            accumulate(point, g)
                            accumulatedPoints.toList()
                        }
                    }
                }
                is Fallible.Failed -> _state.value = Loadable.Loaded(result)
            }
        }
    }

    private fun accumulate(point: PricePoint, granularity: Granularity) {
        val timestampBucket = (point.timestamp / granularity.millis) * granularity.millis
        val bucketed = PricePoint(timestampBucket, point.price)
        val last = accumulatedPoints.lastOrNull()
        if (last != null && last.timestamp == bucketed.timestamp) {
            accumulatedPoints[accumulatedPoints.lastIndex] = last.copy(price = point.price)
        } else {
            if (last != null && bucketed.timestamp < last.timestamp) return
            accumulatedPoints.add(bucketed)
        }
    }

    public override fun onCleared() {
        cancel()
        super.onCleared()
    }
}
