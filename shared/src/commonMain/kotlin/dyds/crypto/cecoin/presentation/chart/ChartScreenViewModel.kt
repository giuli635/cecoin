package dyds.crypto.cecoin.presentation.chart

import dyds.crypto.cecoin.domain.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.presentation.chart.model.ChartData
import dyds.crypto.cecoin.presentation.chart.model.Granularity
import dyds.crypto.cecoin.presentation.utils.AsyncResult
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dyds.crypto.cecoin.domain.model.TradePrice
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
        controller = null
        loadJob?.cancel()
        _state.value = Loadable.Loading

        loadJob = viewModelScope.launch {
            when (val result = getHistoricalPricesUseCase(symbol, g.interval, historicalPointLimit)) {
                is Fallible.Failed -> {
                    _state.value = Loadable.Loaded(result)
                    return@launch
                }
                is Fallible.Success -> {
                    val historical = result.value
                    val c = controllerFactory(g, historical, viewModelScope)
                    c.startStream()
                    controller = c
                    _state.value = Loadable.Loaded(Fallible.Success(c.chartData))
                }
            }
        }
    }

    public override fun onCleared() {
        cancel()
        super.onCleared()
    }
}
