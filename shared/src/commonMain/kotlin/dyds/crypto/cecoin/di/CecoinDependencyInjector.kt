package dyds.crypto.cecoin.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import dyds.crypto.cecoin.data.remote.BinanceCoinPriceSource
import dyds.crypto.cecoin.data.repository.CecoinRepositoryImpl
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.presentation.chart.LiveChartViewModel
import dyds.crypto.cecoin.presentation.search.CoinSearchViewModel

object CecoinDependencyInjector {
    private val binanceClient = BinanceCoinPriceSource()
    private val repository = CecoinRepositoryImpl(binanceClient)
    private val useCase = ObserveTradePricesUseCase(repository)

    @Composable
    fun getSearchViewModel(): CoinSearchViewModel {
        return viewModel {
            CoinSearchViewModel()
        }
    }

    @Composable
    fun getCoinDetailsViewModel(symbol: String): LiveChartViewModel {
        return viewModel {
            LiveChartViewModel(
                observeTradePricesUseCase = useCase,
                symbol = symbol,
            )
        }
    }
}
