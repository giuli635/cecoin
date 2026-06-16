package dyds.crypto.cecoin.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import dyds.crypto.cecoin.data.local.DataStoreFavoriteDataSource
import dyds.crypto.cecoin.data.remote.BinanceCoinHistoricalDataSource
import dyds.crypto.cecoin.data.remote.BinanceCoinListDataSource
import dyds.crypto.cecoin.data.remote.BinanceCoinPriceDataSource
import dyds.crypto.cecoin.data.remote.NewsApiRestDataSource
import dyds.crypto.cecoin.data.repository.CecoinRepositoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import dyds.crypto.cecoin.data.repository.FavoriteRepositoryImpl
import dyds.crypto.cecoin.data.repository.NewsRepositoryImpl
import dyds.crypto.cecoin.domain.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.domain.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveFavoritesUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.domain.usecase.ToggleFavoriteUseCase
import dyds.crypto.cecoin.domain.usecase.GetCryptoNewsUseCase
import dyds.crypto.cecoin.presentation.news.NewsViewModel
import dyds.crypto.cecoin.presentation.chart.ChartDataController
import dyds.crypto.cecoin.presentation.chart.ChartScreenViewModel
import dyds.crypto.cecoin.presentation.chart.GranularityStateHolder
import dyds.crypto.cecoin.presentation.search.CoinSearchViewModel

object CecoinDependencyInjector {
    private val httpClient = HttpClient {
        install(WebSockets)
    }

    private val coinPriceSource = BinanceCoinPriceDataSource(httpClient)
    private val coinHistoricalSource = BinanceCoinHistoricalDataSource(httpClient)
    private val coinListDataSource = BinanceCoinListDataSource(httpClient)

    private val repository = CecoinRepositoryImpl(
        coinPriceSource, coinHistoricalSource, coinListDataSource,
    )
    private val observeTradePricesUseCase = ObserveTradePricesUseCase(repository)
    private val getAvailableSymbolsUseCase = GetAvailableSymbolsUseCase(repository)
    private val getHistoricalPricesUseCase = GetHistoricalPricesUseCase(repository)

    private val favoriteSource = DataStoreFavoriteDataSource()
    private val favoriteRepository = FavoriteRepositoryImpl(favoriteSource)
    private val toggleFavoriteUseCase = ToggleFavoriteUseCase(favoriteRepository)
    private val observeFavoritesUseCase = ObserveFavoritesUseCase(favoriteRepository)

    private val newsApiDataSource = NewsApiRestDataSource(httpClient)
    private val newsRepository = NewsRepositoryImpl(newsApiDataSource)
    private val getCryptoNewsUseCase = GetCryptoNewsUseCase(newsRepository)

    fun dispose() {
        httpClient.close()
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
                controllerFactory = { g, historical, scope ->
                    ChartDataController(
                        observeTradePricesUseCase = observeTradePricesUseCase,
                        granularity = g,
                        scope = scope,
                        symbol = symbol,
                        historical = historical,
                    )
                },
                symbol = symbol,
            )
        }
    }

    @Composable
    fun getNewsViewModel(): NewsViewModel {
        return viewModel {
            NewsViewModel(
                getCryptoNewsUseCase = getCryptoNewsUseCase,
            )
        }
    }
}
