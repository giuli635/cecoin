package dyds.crypto.cecoin.presentation.chart

import dyds.crypto.cecoin.domain.model.toPricePoints
import dyds.crypto.cecoin.domain.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.presentation.chart.model.ChartData
import dyds.crypto.cecoin.presentation.chart.model.Granularity
import dyds.crypto.cecoin.presentation.utils.AsyncResult
import dyds.crypto.cecoin.utils.AppError
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val DEFAULT_HISTORICAL_LIMIT = 200
private const val HISTORICAL_FAILED = "Failed to load historical data"

class ChartScreenViewModel(
    private val getHistoricalPricesUseCase: GetHistoricalPricesUseCase,
    private val controllerFactory: (Granularity) -> ChartDataController,
    private val granularitySource: Flow<Granularity>,
    val symbol: String,
    private val historicalPointLimit: Int = DEFAULT_HISTORICAL_LIMIT,
) : ViewModel() {
    private val _state = MutableStateFlow<AsyncResult<Flow<Fallible<ChartData>>>>(Loadable.Loading)
    val state: StateFlow<AsyncResult<Flow<Fallible<ChartData>>>> = _state.asStateFlow()

    private var controller: ChartDataController? = null
    private var lastGranularity = Granularity.M1
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            granularitySource.collect { g -> loadFresh(g) }
        }
    }

    fun loadPrices() {
        loadFresh(lastGranularity)
    }

    private fun loadFresh(g: Granularity) {
        controller?.cancel()
        controller = null
        loadJob?.cancel()
        lastGranularity = g
        _state.value = Loadable.Loading

        loadJob = viewModelScope.launch {
            try {
                val c = controllerFactory(g)
                try {
                    val historical = getHistoricalPricesUseCase(symbol, g.interval, historicalPointLimit)
                    c.initialize(
                        scope = viewModelScope,
                        historical = historical.toPricePoints(g.millis),
                        g = g,
                    )
                } catch (e: Exception) {
                    c.initialize(
                        scope = viewModelScope,
                        historical = emptyList(),
                        g = g,
                        failed = AppError.GenericError(e, HISTORICAL_FAILED),
                    )
                }
                controller = c
                _state.value = Loadable.Loaded(Fallible.Success(c.chartData))
            } catch (e: Exception) {
                _state.value = Loadable.Loaded(Fallible.Failed(AppError.GenericError(e, HISTORICAL_FAILED)))
            }
        }
    }

    public override fun onCleared() {
        controller?.cancel()
        loadJob?.cancel()
        super.onCleared()
    }
}
