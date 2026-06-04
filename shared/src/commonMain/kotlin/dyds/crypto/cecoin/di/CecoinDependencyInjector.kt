package dyds.crypto.cecoin.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import dyds.crypto.cecoin.data.remote.binance.BinanceCoinListDataSource
import dyds.crypto.cecoin.data.remote.binance.BinanceCoinPriceSource
import dyds.crypto.cecoin.data.remote.binance.BinanceOrderBookSource
import dyds.crypto.cecoin.data.remote.binance.BinancePopularCoinsSource
import dyds.crypto.cecoin.data.remote.binance.proxy.BinanceCoinListDataSourceProxy
import dyds.crypto.cecoin.data.remote.binance.proxy.BinancePopularCoinsSourceProxy
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
import dyds.crypto.cecoin.domain.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.presentation.chart.LiveChartViewModel
import dyds.crypto.cecoin.presentation.search.CoinSearchViewModel

object CecoinDependencyInjector {
    private val binancePriceSource = BinanceCoinPriceSource()
    private val binanceOrderBookSource = BinanceOrderBookSource()
    private val coinCapPriceSource = CoinCapPriceSource()
    private val coinCapOrderBookSource = CoinCapOrderBookSource()
    private val binanceCoinListDataSource = BinanceCoinListDataSource()
    private val binancePopularCoinsSource = BinancePopularCoinsSource()

    private val binancePriceProxy = BinancePriceSourceProxy(binancePriceSource)
    private val binanceOrderBookProxy = BinanceOrderBookSourceProxy(binanceOrderBookSource)
    private val binanceCoinListProxy = BinanceCoinListDataSourceProxy(binanceCoinListDataSource)
    private val binancePopularCoinsProxy = BinancePopularCoinsSourceProxy(binancePopularCoinsSource)
    private val coinCapPriceProxy = CoinCapPriceSourceProxy(coinCapPriceSource)
    private val coinCapOrderBookProxy = CoinCapOrderBookSourceProxy(coinCapOrderBookSource)

    private val priceBroker = CoinPriceSourceBroker(binancePriceProxy, coinCapPriceProxy)
    private val orderBookBroker = CoinOrderBookSourceBroker(binanceOrderBookProxy, coinCapOrderBookProxy)
    private val coinListBroker = CoinListDataSourceBroker(binanceCoinListProxy, binancePopularCoinsProxy)

    private val repository = CryptoRepositoryImpl(priceBroker, orderBookBroker, coinListBroker)
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
