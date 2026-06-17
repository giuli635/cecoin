package dyds.crypto.cecoin.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dyds.crypto.cecoin.data.local.DataStoreFavoriteDataSource
import dyds.crypto.cecoin.data.remote.BinanceCoinHistoricalDataSource
import dyds.crypto.cecoin.data.remote.BinanceCoinListDataSource
import dyds.crypto.cecoin.data.remote.BinanceCoinPriceDataSource
import dyds.crypto.cecoin.data.remote.NewsApiRestDataSource
import dyds.crypto.cecoin.data.repository.CecoinRepositoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import dyds.crypto.cecoin.data.repository.FavoriteRepositoryImpl
import dyds.crypto.cecoin.utils.ErrorClassifier
import dyds.crypto.cecoin.data.repository.NewsRepositoryImpl
import dyds.crypto.cecoin.domain.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.domain.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveFavoritesUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveFavoritesUseCaseImpl
import dyds.crypto.cecoin.domain.usecase.ToggleFavoriteUseCase
import dyds.crypto.cecoin.domain.usecase.ToggleFavoriteUseCaseImpl
import dyds.crypto.cecoin.domain.usecase.GetAvailableSymbolsUseCaseImpl
import dyds.crypto.cecoin.domain.usecase.GetHistoricalPricesUseCaseImpl
import dyds.crypto.cecoin.domain.usecase.GetCryptoNewsUseCase
import dyds.crypto.cecoin.domain.usecase.GetCryptoNewsUseCaseImpl
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCaseImpl
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

    private val repository = CecoinRepositoryImpl(
        coinPriceSource, coinHistoricalSource, coinListDataSource,
    )
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
        observeTradePricesUseCase = ObserveTradePricesUseCaseImpl(repository, classifier)
        getAvailableSymbolsUseCase = GetAvailableSymbolsUseCaseImpl(repository, classifier)
        getHistoricalPricesUseCase = GetHistoricalPricesUseCaseImpl(repository, classifier)
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
