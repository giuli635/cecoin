package dyds.crypto.cecoin.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import dyds.crypto.cecoin.data.remote.BinanceCoinPriceSource
import dyds.crypto.cecoin.data.remote.BinanceCoinListDataSource
import dyds.crypto.cecoin.data.repository.CecoinRepositoryImpl
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.domain.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.presentation.chart.LiveChartViewModel
import dyds.crypto.cecoin.presentation.search.CoinSearchViewModel

object CecoinDependencyInjector {
    private val binanceClient = BinanceCoinPriceSource()
    private val binanceCoinListDataSource = BinanceCoinListDataSource()
    private val repository = CecoinRepositoryImpl(binanceClient, binanceCoinListDataSource)
    private val observeTradePricesUseCase = ObserveTradePricesUseCase(repository)
    private val getAvailableSymbolsUseCase = GetAvailableSymbolsUseCase(repository)

    @Composable
    fun getSearchViewModel(): CoinSearchViewModel {
        return viewModel {
            CoinSearchViewModel(getAvailableSymbolsUseCase)
        }
    }

    @Composable
    fun getCoinDetailsViewModel(symbol: String): LiveChartViewModel {
        return viewModel {
            LiveChartViewModel(
                observeTradePricesUseCase = observeTradePricesUseCase,
                symbol = symbol,
            )
        }
    }
}
