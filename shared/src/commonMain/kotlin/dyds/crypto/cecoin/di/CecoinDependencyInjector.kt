package dyds.crypto.cecoin.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dyds.crypto.cecoin.data.chart.datasource.BinanceCoinHistoricalDataSource
import dyds.crypto.cecoin.data.chart.datasource.BinanceCoinPriceDataSource
import dyds.crypto.cecoin.data.chart.repository.ChartRepositoryImpl
import dyds.crypto.cecoin.data.news.datasource.NewsApiRestDataSource
import dyds.crypto.cecoin.data.news.repository.NewsRepositoryImpl
import dyds.crypto.cecoin.data.search.datasource.BinanceCoinListDataSource
import dyds.crypto.cecoin.data.search.datasource.DataStoreFavoriteDataSource
import dyds.crypto.cecoin.data.search.repository.FavoriteRepositoryImpl
import dyds.crypto.cecoin.data.search.repository.SearchRepositoryImpl
import dyds.crypto.cecoin.domain.chart.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.domain.chart.usecase.GetHistoricalPricesUseCaseImpl
import dyds.crypto.cecoin.domain.chart.usecase.ObserveTradePricesUseCaseImpl
import dyds.crypto.cecoin.domain.news.usecase.GetCryptoNewsUseCase
import dyds.crypto.cecoin.domain.news.usecase.GetCryptoNewsUseCaseImpl
import dyds.crypto.cecoin.domain.search.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.domain.search.usecase.GetAvailableSymbolsUseCaseImpl
import dyds.crypto.cecoin.domain.search.usecase.ObserveFavoritesUseCase
import dyds.crypto.cecoin.domain.search.usecase.ObserveFavoritesUseCaseImpl
import dyds.crypto.cecoin.domain.search.usecase.ToggleFavoriteUseCase
import dyds.crypto.cecoin.domain.search.usecase.ToggleFavoriteUseCaseImpl
import dyds.crypto.cecoin.utils.error.ErrorClassifier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import dyds.crypto.cecoin.presentation.news.NewsViewModel
import dyds.crypto.cecoin.presentation.chart.ChartDataController
import dyds.crypto.cecoin.presentation.chart.ChartScreenViewModel
import dyds.crypto.cecoin.presentation.chart.GranularityStateHolder
import dyds.crypto.cecoin.presentation.chart.util.PriceAccumulatorImpl
import dyds.crypto.cecoin.presentation.search.CoinSearchViewModel

object CecoinDependencyInjector {
    lateinit var errorClassifier: ErrorClassifier
        private set

    private val httpClient = HttpClient {
        install(WebSockets)
    }

    private val coinPriceSource = BinanceCoinPriceDataSource(httpClient)
    private val coinHistoricalSource = BinanceCoinHistoricalDataSource(httpClient)
    private val coinListDataSource = BinanceCoinListDataSource(httpClient)

    private val searchRepository = SearchRepositoryImpl(coinListDataSource)
    private val chartRepository = ChartRepositoryImpl(coinPriceSource, coinHistoricalSource)
    private val newsApiDataSource = NewsApiRestDataSource(httpClient)
    private val newsRepository = NewsRepositoryImpl(newsApiDataSource)

    private lateinit var observeTradePricesUseCase: ObserveTradePricesUseCaseImpl
    private lateinit var getAvailableSymbolsUseCase: GetAvailableSymbolsUseCase
    private lateinit var getHistoricalPricesUseCase: GetHistoricalPricesUseCase
    private lateinit var getCryptoNewsUseCase: GetCryptoNewsUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var observeFavoritesUseCase: ObserveFavoritesUseCase
    private lateinit var favoriteSource: DataStoreFavoriteDataSource
    private lateinit var favoriteRepository: FavoriteRepositoryImpl

    fun configure(dataStore: DataStore<Preferences>, classifier: ErrorClassifier) {
        errorClassifier = classifier
        favoriteSource = DataStoreFavoriteDataSource(dataStore)
        favoriteRepository = FavoriteRepositoryImpl(favoriteSource)
        observeTradePricesUseCase = ObserveTradePricesUseCaseImpl(chartRepository, classifier)
        getAvailableSymbolsUseCase = GetAvailableSymbolsUseCaseImpl(searchRepository, classifier)
        getHistoricalPricesUseCase = GetHistoricalPricesUseCaseImpl(chartRepository, classifier)
        getCryptoNewsUseCase = GetCryptoNewsUseCaseImpl(newsRepository, classifier)
        toggleFavoriteUseCase = ToggleFavoriteUseCaseImpl(favoriteRepository, classifier)
        observeFavoritesUseCase = ObserveFavoritesUseCaseImpl(favoriteRepository)
    }

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
                controllerFactory = { granularity, historical, scope ->
                    ChartDataController(
                        observeTradePricesUseCase = observeTradePricesUseCase,
                        priceAccumulator = PriceAccumulatorImpl(granularity, historical),
                        scope = scope,
                        symbol = symbol,
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
