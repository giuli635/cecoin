package dyds.crypto.cecoin.core.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.chart.data.datasource.BinanceCoinHistoricalDataSource
import dyds.crypto.cecoin.chart.data.datasource.BinanceCoinPriceDataSource
import dyds.crypto.cecoin.chart.data.repository.PriceRepositoryImpl
import dyds.crypto.cecoin.news.data.datasource.NewsApiRestDataSource
import dyds.crypto.cecoin.news.data.repository.NewsRepositoryImpl
import dyds.crypto.cecoin.search.data.datasource.BinanceCoinListDataSource
import dyds.crypto.cecoin.search.data.datasource.DataStoreFavoriteDataSource
import dyds.crypto.cecoin.search.data.repository.FavoriteRepositoryImpl
import dyds.crypto.cecoin.search.data.repository.SearchRepositoryImpl
import dyds.crypto.cecoin.chart.domain.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.chart.domain.usecase.GetHistoricalPricesUseCaseImpl
import dyds.crypto.cecoin.chart.domain.usecase.ObservePricesUseCaseImpl
import dyds.crypto.cecoin.news.domain.usecase.GetCryptoNewsUseCase
import dyds.crypto.cecoin.news.domain.usecase.GetCryptoNewsUseCaseImpl
import dyds.crypto.cecoin.search.domain.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.search.domain.usecase.GetAvailableSymbolsUseCaseImpl
import dyds.crypto.cecoin.search.domain.usecase.ObserveFavoritesUseCase
import dyds.crypto.cecoin.search.domain.usecase.ObserveFavoritesUseCaseImpl
import dyds.crypto.cecoin.search.domain.usecase.ToggleFavoriteUseCase
import dyds.crypto.cecoin.search.domain.usecase.ToggleFavoriteUseCaseImpl
import dyds.crypto.cecoin.core.data.caching.CachedDataSource
import dyds.crypto.cecoin.core.domain.error.ErrorClassifier
import kotlin.time.Duration.Companion.minutes
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import cecoin.shared.generated.resources.*
import org.jetbrains.compose.resources.getString
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

    private val coinListCache = CachedDataSource(
        fetchBlock = coinListDataSource::fetchSymbols,
        cacheTtl = 5.minutes,
    )
    private val newsApiDataSource = NewsApiRestDataSource(httpClient)
    private val newsCache = CachedDataSource(
        fetchBlock = newsApiDataSource::fetchCryptoNews,
        cacheTtl = 2.minutes,
    )

    private val searchRepository = SearchRepositoryImpl(coinListDataSource, coinListCache)
    private val chartRepository = PriceRepositoryImpl(coinPriceSource, coinHistoricalSource)
    private val newsRepository = NewsRepositoryImpl(newsApiDataSource, newsCache)

    private lateinit var observePricesUseCase: ObservePricesUseCaseImpl
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
        observePricesUseCase = ObservePricesUseCaseImpl(
            chartRepository, classifier,
            lazyMessage = { getString(Res.string.error_live_stream_failed) },
        )
        getAvailableSymbolsUseCase = GetAvailableSymbolsUseCaseImpl(
            searchRepository, classifier,
            lazyMessage = { getString(Res.string.error_load_symbols) },
        )
        getHistoricalPricesUseCase = GetHistoricalPricesUseCaseImpl(
            chartRepository, classifier,
            lazyMessage = { getString(Res.string.error_historical_data) },
        )
        getCryptoNewsUseCase = GetCryptoNewsUseCaseImpl(
            newsRepository, classifier,
            lazyMessage = { getString(Res.string.error_load_news) },
        )
        toggleFavoriteUseCase = ToggleFavoriteUseCaseImpl(
            favoriteRepository, classifier,
            lazyMessage = { getString(Res.string.error_toggle_favorite) },
        )
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
        symbol: CryptoSymbol,
    ): ChartScreenViewModel {
        return viewModel {
            ChartScreenViewModel(
                getHistoricalPricesUseCase = getHistoricalPricesUseCase,
                observePricesUseCase = observePricesUseCase,
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
