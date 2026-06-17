package dyds.crypto.cecoin.core.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dyds.crypto.cecoin.chart.data.datasource.BinanceCoinHistoricalDataSource
import dyds.crypto.cecoin.chart.data.datasource.BinanceCoinPriceDataSource
import dyds.crypto.cecoin.chart.data.repository.ChartRepositoryImpl
import dyds.crypto.cecoin.news.data.datasource.NewsApiRestDataSource
import dyds.crypto.cecoin.news.data.repository.NewsRepositoryImpl
import dyds.crypto.cecoin.search.data.datasource.BinanceCoinListDataSource
import dyds.crypto.cecoin.search.data.datasource.DataStoreFavoriteDataSource
import dyds.crypto.cecoin.search.data.repository.FavoriteRepositoryImpl
import dyds.crypto.cecoin.search.data.repository.SearchRepositoryImpl
import dyds.crypto.cecoin.chart.domain.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.chart.domain.usecase.GetHistoricalPricesUseCaseImpl
import dyds.crypto.cecoin.chart.domain.usecase.ObserveTradePricesUseCaseImpl
import dyds.crypto.cecoin.news.domain.usecase.GetCryptoNewsUseCase
import dyds.crypto.cecoin.news.domain.usecase.GetCryptoNewsUseCaseImpl
import dyds.crypto.cecoin.search.domain.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.search.domain.usecase.GetAvailableSymbolsUseCaseImpl
import dyds.crypto.cecoin.search.domain.usecase.ObserveFavoritesUseCase
import dyds.crypto.cecoin.search.domain.usecase.ObserveFavoritesUseCaseImpl
import dyds.crypto.cecoin.search.domain.usecase.ToggleFavoriteUseCase
import dyds.crypto.cecoin.search.domain.usecase.ToggleFavoriteUseCaseImpl
import dyds.crypto.cecoin.core.utils.error.ErrorClassifier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import dyds.crypto.cecoin.news.presentation.NewsViewModel
import dyds.crypto.cecoin.chart.presentation.ChartScreenViewModel
import dyds.crypto.cecoin.chart.presentation.GranularityStateHolder
import dyds.crypto.cecoin.search.presentation.CoinSearchViewModel

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
                observeTradePricesUseCase = observeTradePricesUseCase,
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
