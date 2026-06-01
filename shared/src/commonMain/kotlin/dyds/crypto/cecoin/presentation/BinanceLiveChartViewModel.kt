package dyds.crypto.cecoin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds

class BinanceLiveChartViewModel(
    observeTradePricesUseCase: ObserveTradePricesUseCase,
    val symbol: String,
    private val maxPoints: Int = 200,
    private val retryDelayMillis: Long = 1_000L,
) : ViewModel() {

    val uiState = observeTradePricesUseCase(symbol)
        .retryWhen { cause, _ ->
            if (cause is CancellationException) {
                false
            } else {
                delay(retryDelayMillis.milliseconds)
                true
            }
        }
        .scan(BinanceLiveChartUiState(symbol = symbol)) { state, tradePrice ->
            state.copy(
                symbol = tradePrice.symbol,
                prices = (state.prices + tradePrice.price).takeLast(maxPoints),
                connectionState = ConnectionState.Connected,
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            BinanceLiveChartUiState(symbol = symbol),
        )
}
