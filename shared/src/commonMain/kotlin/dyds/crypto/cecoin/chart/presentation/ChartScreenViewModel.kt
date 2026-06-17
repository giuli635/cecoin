package dyds.crypto.cecoin.chart.presentation

import dyds.crypto.cecoin.chart.domain.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.chart.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.chart.presentation.model.ChartData
import dyds.crypto.cecoin.chart.presentation.model.Granularity
import dyds.crypto.cecoin.core.presentation.utils.AsyncResult
import dyds.crypto.cecoin.core.presentation.utils.launchLoadable
import dyds.crypto.cecoin.core.utils.state.Loadable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val DEFAULT_HISTORICAL_LIMIT = 200

class ChartScreenViewModel(
    private val getHistoricalPricesUseCase: GetHistoricalPricesUseCase,
    private val observeTradePricesUseCase: ObserveTradePricesUseCase,
    val symbol: String,
    private val historicalPointLimit: Int = DEFAULT_HISTORICAL_LIMIT,
) : ViewModel() {
    private val controller = ChartDataController(
        getHistoricalPricesUseCase,
        observeTradePricesUseCase,
        viewModelScope,
    )
    private val _state = MutableStateFlow<AsyncResult<Flow<ChartData>>>(Loadable.Loading)
    val state: StateFlow<AsyncResult<Flow<ChartData>>> = _state.asStateFlow()

    private var loadJob: Job? = null

    fun cancel() {
        loadJob?.cancel()
        controller.cancel()
    }

    fun load(g: Granularity) {
        loadJob?.cancel()
        loadJob = launchLoadable(_state) {
            controller.observe(symbol, g, historicalPointLimit)
        }
    }

    public override fun onCleared() {
        cancel()
        super.onCleared()
    }
}
