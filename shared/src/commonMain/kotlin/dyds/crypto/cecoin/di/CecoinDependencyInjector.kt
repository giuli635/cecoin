package dyds.crypto.cecoin.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import dyds.crypto.cecoin.data.local.FavoriteLocalSource
import dyds.crypto.cecoin.data.local.createFavoriteStorage
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
import dyds.crypto.cecoin.presentation.chart.ChartDataController
import dyds.crypto.cecoin.presentation.chart.ChartScreenViewModel
import dyds.crypto.cecoin.presentation.chart.GranularityStateHolder
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

    private val favoriteStorage = createFavoriteStorage()
    private val favoriteSource = FavoriteLocalSource(favoriteStorage)
    private val favoriteRepository = FavoriteRepositoryImpl(favoriteSource)
    private val toggleFavoriteUseCase = ToggleFavoriteUseCase(favoriteRepository)
    private val observeFavoritesUseCase = ObserveFavoritesUseCase(favoriteRepository)

    fun dispose() {
        coinPriceSource.close()
        coinHistoricalSource.close()
        coinListDataSource.close()
    }

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
    fun getGranularityStateHolder(): GranularityStateHolder {
        return remember { GranularityStateHolder() }
    }

    @Composable
    fun getCoinDetailsViewModel(
        symbol: String,
    ): ChartScreenViewModel {
        return viewModel {
            ChartScreenViewModel(
                getHistoricalPricesUseCase = getHistoricalPricesUseCase,
                controllerFactory = { g ->
                    ChartDataController(
                        observeTradePricesUseCase = observeTradePricesUseCase,
                        symbol = symbol,
                    )
                },
                symbol = symbol,
            )
        }
    }
}
