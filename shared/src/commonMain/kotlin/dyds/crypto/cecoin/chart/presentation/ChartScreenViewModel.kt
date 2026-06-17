package dyds.crypto.cecoin.chart.presentation

import dyds.crypto.cecoin.chart.domain.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.chart.domain.usecase.ObservePricesUseCase
import dyds.crypto.cecoin.chart.presentation.model.ChartData
import dyds.crypto.cecoin.chart.presentation.model.Granularity
import dyds.crypto.cecoin.chart.presentation.util.PriceAccumulatorFactory
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.presentation.utils.AsyncResult
import dyds.crypto.cecoin.core.utils.state.Fallible
import dyds.crypto.cecoin.core.utils.state.Loadable
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
    private val priceAccumulatorFactory: PriceAccumulatorFactory,
) : ViewModel() {
    private val _chartData = MutableStateFlow<ChartData>(Fallible.Success(emptyList()))
    val chartData: StateFlow<ChartData> = _chartData.asStateFlow()

    private val _state = MutableStateFlow<AsyncResult<Flow<ChartData>>>(Loadable.Loading)
    val state: StateFlow<AsyncResult<Flow<ChartData>>> = _state.asStateFlow()

    private var loadJob: Job? = null

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
                    val accumulator = priceAccumulatorFactory(g, result.value)
                    _chartData.value = Fallible.Success(accumulator.snapshot())
                    _state.value = Loadable.Loaded(Fallible.Success(_chartData.asStateFlow()))
                    observePricesUseCase(symbol).collect { f ->
                        _chartData.value = f.map { accumulator.accumulateAndSnapshot(it) }
                    }
                }
                is Fallible.Failed -> _state.value = Loadable.Loaded(result)
            }
        }
    }

    public override fun onCleared() {
        cancel()
        super.onCleared()
    }
}
