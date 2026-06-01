package dyds.crypto.cecoin.di

import dyds.crypto.cecoin.data.remote.BinanceStreamClient
import dyds.crypto.cecoin.data.repository.DefaultTradePriceRepository
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.presentation.BinanceLiveChartViewModel
import dyds.crypto.cecoin.presentation.CoinSearchViewModel

class AppDependencies(
    val searchViewModel: CoinSearchViewModel,
    private val binanceClient: BinanceStreamClient,
    private val useCase: ObserveTradePricesUseCase,
    private val closeActions: List<() -> Unit>,
) {
    fun createChartViewModel(symbol: String): BinanceLiveChartViewModel {
        return BinanceLiveChartViewModel(useCase, symbol)
    }

    fun close() {
        closeActions.forEach { closeAction ->
            runCatching { closeAction() }
        }
    }
}

fun createAppDependencies(binanceClient: BinanceStreamClient): AppDependencies {
    val repository = DefaultTradePriceRepository(binanceClient)
    val useCase = ObserveTradePricesUseCase(repository)
    val searchViewModel = CoinSearchViewModel()

    return AppDependencies(
        searchViewModel = searchViewModel,
        binanceClient = binanceClient,
        useCase = useCase,
        closeActions = listOf(binanceClient::close),
    )
}





