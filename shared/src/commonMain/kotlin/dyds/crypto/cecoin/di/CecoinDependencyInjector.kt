package dyds.crypto.cecoin.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import dyds.crypto.cecoin.data.local.FavoriteLocalSource
import dyds.crypto.cecoin.data.remote.BinanceCoinHistoricalSource
import dyds.crypto.cecoin.data.remote.BinanceCoinListDataSource
import dyds.crypto.cecoin.data.remote.BinanceCoinPriceSource
import dyds.crypto.cecoin.data.repository.CecoinRepositoryImpl
import dyds.crypto.cecoin.data.repository.FavoriteRepositoryImpl
import dyds.crypto.cecoin.domain.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.domain.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveFavoritesUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.domain.usecase.ToggleFavoriteUseCase
import dyds.crypto.cecoin.presentation.chart.LiveChartViewModel
import dyds.crypto.cecoin.presentation.search.CoinSearchViewModel

object CecoinDependencyInjector {
    private val coinPriceSource = BinanceCoinPriceSource()
    private val coinHistoricalSource = BinanceCoinHistoricalSource()
    private val coinListDataSource = BinanceCoinListDataSource()

    private val repository = CecoinRepositoryImpl(
        coinPriceSource, coinHistoricalSource, coinListDataSource,
    )
    private val observeTradePricesUseCase = ObserveTradePricesUseCase(repository)
    private val getAvailableSymbolsUseCase = GetAvailableSymbolsUseCase(repository)
    private val getHistoricalPricesUseCase = GetHistoricalPricesUseCase(repository)

    private val favoriteSource = FavoriteLocalSource()
    private val favoriteRepository = FavoriteRepositoryImpl(favoriteSource)
    private val toggleFavoriteUseCase = ToggleFavoriteUseCase(favoriteRepository)
    private val observeFavoritesUseCase = ObserveFavoritesUseCase(favoriteRepository)

    @Composable
    fun getSearchViewModel(): CoinSearchViewModel {
        return viewModel {
            CoinSearchViewModel(
                getAvailableSymbolsUseCase = getAvailableSymbolsUseCase,
                toggleFavoriteUseCase = toggleFavoriteUseCase,
                observeFavoritesUseCase = observeFavoritesUseCase,
            )
        }
    }

    @Composable
    fun getCoinDetailsViewModel(symbol: String): LiveChartViewModel {
        return viewModel {
            LiveChartViewModel(
                getHistoricalPricesUseCase = getHistoricalPricesUseCase,
                observeTradePricesUseCase = observeTradePricesUseCase,
                symbol = symbol,
            )
        }
    }
}
