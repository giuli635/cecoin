package dyds.crypto.cecoin.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import dyds.crypto.cecoin.data.local.FavoriteLocalSource
import dyds.crypto.cecoin.data.remote.binance.BinanceCoinHistoricalSource
import dyds.crypto.cecoin.data.remote.binance.BinanceCoinListDataSource
import dyds.crypto.cecoin.data.remote.binance.BinanceCoinPriceSource
import dyds.crypto.cecoin.data.remote.binance.BinanceOrderBookSource
import dyds.crypto.cecoin.data.remote.binance.proxy.BinanceCoinListDataSourceProxy
import dyds.crypto.cecoin.data.remote.binance.proxy.BinanceOrderBookSourceProxy
import dyds.crypto.cecoin.data.remote.binance.proxy.BinancePriceSourceProxy
import dyds.crypto.cecoin.data.remote.broker.CoinListDataSourceBroker
import dyds.crypto.cecoin.data.remote.broker.CoinOrderBookSourceBroker
import dyds.crypto.cecoin.data.remote.broker.CoinPriceSourceBroker
import dyds.crypto.cecoin.data.remote.coincap.CoinCapOrderBookSource
import dyds.crypto.cecoin.data.remote.coincap.CoinCapPriceSource
import dyds.crypto.cecoin.data.remote.coincap.proxy.CoinCapOrderBookSourceProxy
import dyds.crypto.cecoin.data.remote.coincap.proxy.CoinCapPriceSourceProxy
import dyds.crypto.cecoin.data.repository.CryptoRepositoryImpl
import dyds.crypto.cecoin.data.repository.FavoriteRepositoryImpl
import dyds.crypto.cecoin.domain.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.domain.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveFavoritesUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.domain.usecase.ToggleFavoriteUseCase
import dyds.crypto.cecoin.presentation.chart.LiveChartViewModel
import dyds.crypto.cecoin.presentation.search.CoinSearchViewModel

object CecoinDependencyInjector {
    private val binancePriceSource = BinanceCoinPriceSource()
    private val binanceHistoricalSource = BinanceCoinHistoricalSource()
    private val binanceOrderBookSource = BinanceOrderBookSource()
    private val coinCapPriceSource = CoinCapPriceSource()
    private val coinCapOrderBookSource = CoinCapOrderBookSource()
    private val binanceCoinListDataSource = BinanceCoinListDataSource()

    private val binancePriceProxy = BinancePriceSourceProxy(binancePriceSource)
    private val binanceOrderBookProxy = BinanceOrderBookSourceProxy(binanceOrderBookSource)
    private val binanceCoinListProxy = BinanceCoinListDataSourceProxy(binanceCoinListDataSource)
    private val coinCapPriceProxy = CoinCapPriceSourceProxy(coinCapPriceSource)
    private val coinCapOrderBookProxy = CoinCapOrderBookSourceProxy(coinCapOrderBookSource)

    private val priceBroker = CoinPriceSourceBroker(binancePriceProxy, coinCapPriceProxy)
    private val orderBookBroker = CoinOrderBookSourceBroker(binanceOrderBookProxy, coinCapOrderBookProxy)
    private val coinListBroker = CoinListDataSourceBroker(binanceCoinListProxy)

    private val repository = CryptoRepositoryImpl(
        priceBroker, binanceHistoricalSource, orderBookBroker, coinListBroker,
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
