package dyds.crypto.cecoin.presentation.chart

import dyds.crypto.cecoin.domain.chart.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.presentation.chart.model.ChartData
import dyds.crypto.cecoin.presentation.chart.model.Granularity
import dyds.crypto.cecoin.presentation.utils.AsyncResult
import dyds.crypto.cecoin.utils.state.Fallible
import dyds.crypto.cecoin.utils.state.Loadable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dyds.crypto.cecoin.domain.chart.model.TradePrice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val DEFAULT_HISTORICAL_LIMIT = 200

class ChartScreenViewModel(
    private val getHistoricalPricesUseCase: GetHistoricalPricesUseCase,
    private val controllerFactory: (Granularity, List<TradePrice>, CoroutineScope) -> ChartDataController,
    val symbol: String,
    private val historicalPointLimit: Int = DEFAULT_HISTORICAL_LIMIT,
) : ViewModel() {
    private val _state = MutableStateFlow<AsyncResult<Flow<ChartData>>>(Loadable.Loading)
    val state: StateFlow<AsyncResult<Flow<ChartData>>> = _state.asStateFlow()

    private var controller: ChartDataController? = null
    private var loadJob: Job? = null

    fun cancel() {
        controller?.cancel()
        loadJob?.cancel()
    }

    fun load(g: Granularity) {
        controller?.cancel()
        loadJob?.cancel()
        _state.value = Loadable.Loading

        loadJob = viewModelScope.launch {
            _state.value = Loadable.Loaded(
                getHistoricalPricesUseCase(symbol, g.interval, historicalPointLimit)
                    .map { prices ->
                        controllerFactory(g, prices, viewModelScope)
                            .also { c ->
                                c.startStream()
                                controller = c
                            }
                            .chartData
                    }
            )
        }
    }

    public override fun onCleared() {
        cancel()
        super.onCleared()
    }
}
