package dyds.crypto.cecoin.presentation.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dyds.crypto.cecoin.domain.usecase.IsFavoriteUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.domain.usecase.ToggleFavoriteUseCase
import dyds.crypto.cecoin.utils.AppError
import dyds.crypto.cecoin.utils.Fallible
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class LiveChartViewModel(
    private val observeTradePricesUseCase: ObserveTradePricesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase,
    val symbol: String,
    private val maxPoints: Int = 200,
    private val retryDelayMillis: Long = 1_000L,
) : ViewModel() {
    private val _uiState = MutableSharedFlow<ChartState>(replay = 1)
    val uiState: Flow<ChartState> = _uiState.asSharedFlow()

    val isFavorite: StateFlow<Boolean> = isFavoriteUseCase(symbol)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    private var priceJob: Job? = null

    fun toggleFavorite() {
        viewModelScope.launch {
            toggleFavoriteUseCase(symbol)
        }
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    fun loadPrices() {
        priceJob?.cancel()
        priceJob = viewModelScope.launch {
            observeTradePricesUseCase(symbol)
                .retryWhen { cause, _ ->
                    if (cause is CancellationException) return@retryWhen false

                    _uiState.emit(
                        Fallible.Failed(
                            AppError.GenericError(cause, "Error observing trade prices")
                        )
                    )

                    delay(retryDelayMillis.milliseconds)
                    true
                }
                .sample(33.milliseconds)
                .scan(emptyList<Double>()) { prices, tradePrice ->
                    (prices + tradePrice.price).takeLast(maxPoints)
                }
                .drop(1)
                .collect { prices ->
                    _uiState.emit(
                        Fallible.Success(
                            PricePoints(prices)
                        )
                    )
                }
        }
    }
}
