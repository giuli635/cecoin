package dyds.crypto.cecoin.presentation.chart

import dyds.crypto.cecoin.domain.chart.model.PricePoint
import dyds.crypto.cecoin.domain.chart.model.TradePrice
import dyds.crypto.cecoin.domain.chart.usecase.FakeGetHistoricalPricesUseCase
import dyds.crypto.cecoin.presentation.chart.model.Granularity
import dyds.crypto.cecoin.presentation.chart.util.PriceAccumulatorImpl
import dyds.crypto.cecoin.utils.state.Fallible
import dyds.crypto.cecoin.utils.state.Loadable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ChartScreenViewModelTest {

    private suspend fun ChartScreenViewModel.awaitChartData(): List<PricePoint> {
        val state = state.first { it !is Loadable.Loading }
        val loaded = state as Loadable.Loaded
        val success = loaded.value as Fallible.Success
        val flow: Flow<Fallible<List<PricePoint>>> = success.value
        val fallible = flow.first { it is Fallible.Success }
        val successValue = fallible as Fallible.Success
        return successValue.value
    }

    private suspend fun ChartScreenViewModel.waitForPoints(price: Double): List<PricePoint> {
        val state = state.first()
        val loaded = state as Loadable.Loaded
        val success = loaded.value as Fallible.Success
        val flow: Flow<Fallible<List<PricePoint>>> = success.value
        val emission = flow.first { it is Fallible.Success && it.value.any { p -> p.price == price } }
        val successValue = emission as Fallible.Success
        return successValue.value
    }

    private suspend fun waitForPriceStillVisible(viewModel: ChartScreenViewModel, price: Double): List<PricePoint> {
        val state = viewModel.state.first()
        val loaded = state as Loadable.Loaded
        val success = loaded.value as Fallible.Success
        val flow: Flow<Fallible<List<PricePoint>>> = success.value
        val emission = flow.first { it is Fallible.Success }
        val successValue = emission as Fallible.Success
        return successValue.value
    }

    private data class VMScope(
        val viewModel: ChartScreenViewModel,
        val tradeUseCase: FakeObserveTradePricesUseCase,
        val historicalUseCase: FakeGetHistoricalPricesUseCase,
    )

    private fun createViewModel(
        historicalPrices: List<TradePrice> = emptyList(),
        historicalException: Throwable? = null,
        tradeException: Throwable? = null,
        historicalPointLimit: Int = 200,
    ): VMScope {
        val fakeHistorical = FakeGetHistoricalPricesUseCase(prices = historicalPrices, exception = historicalException)
        val fakeTradeUseCase = FakeObserveTradePricesUseCase(exception = tradeException)
        val viewModel = ChartScreenViewModel(
            getHistoricalPricesUseCase = fakeHistorical,
            controllerFactory = { g, historical, scope ->
                ChartDataController(
                    observeTradePricesUseCase = fakeTradeUseCase,
                    priceAccumulator = PriceAccumulatorImpl(g, historical),
                    symbol = "BTCUSDT",
                    scope = scope,
                )
            },
            symbol = "BTCUSDT",
            historicalPointLimit = historicalPointLimit,
        )
        return VMScope(viewModel, fakeTradeUseCase, fakeHistorical)
    }

    @Test
    fun `loads historical data and emits success`() = runTest {
        val (viewModel, _, _) = createViewModel(historicalPrices = listOf(
            TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
            TradePrice("BTCUSDT", PricePoint(60_000L, 51000.0)),
        ))

        viewModel.load(Granularity.M1)
        val chartData = viewModel.awaitChartData()

        assertTrue(chartData.isNotEmpty())
        assertEquals(51000.0, chartData.last().price)
    }

    @Test
    fun `emits failure when historical fetch fails`() = runTest {
        val (viewModel, _, _) = createViewModel(
            historicalPrices = emptyList(),
            historicalException = RuntimeException("Network error"),
        )

        viewModel.load(Granularity.M1)
        val state = viewModel.state.first { it !is Loadable.Loading }
        assertIs<Loadable.Loaded<*>>(state)
        val loaded = state as Loadable.Loaded
        assertIs<Fallible.Failed>(loaded.value)
    }

    @Test
    fun `sets stream state as Loading`() = runTest {
        val (viewModel, _, _) = createViewModel()

        viewModel.load(Granularity.M1)
        val chartData = viewModel.awaitChartData()

        assertTrue(chartData.isEmpty())
    }

    @Test
    fun `granularity change triggers new load`() = runTest {
        val (viewModel, _, _) = createViewModel()

        viewModel.load(Granularity.M1)
        viewModel.awaitChartData()

        viewModel.load(Granularity.M5)
        val data = viewModel.awaitChartData()
        assertTrue(data.isEmpty())
    }

    @Test
    fun `same granularity value does not reload`() = runTest {
        val (viewModel, _, _) = createViewModel(historicalPrices = listOf(
            TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
        ))

        viewModel.load(Granularity.M1)
        viewModel.awaitChartData()

        viewModel.load(Granularity.M1)
        val state = viewModel.state.first { it !is Loadable.Loading }
        assertIs<Loadable.Loaded<*>>(state)
    }

    @Test
    fun `historical limit is passed through`() = runTest {
        val (viewModel, _, historicalUseCase) = createViewModel(
            historicalPrices = emptyList(),
            historicalPointLimit = 50,
        )

        viewModel.load(Granularity.M1)
        viewModel.awaitChartData()

        assertEquals(50, historicalUseCase.lastLimit)
    }

    @Test
    fun `live prices update lastPrice`() = runTest {
        val (viewModel, tradeUseCase, _) = createViewModel(historicalPrices = listOf(
            TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
        ))

        viewModel.load(Granularity.M1)
        val initialData = viewModel.awaitChartData()
        assertEquals(50000.0, initialData.last().price)

        tradeUseCase.emitted.send(TradePrice("BTCUSDT", PricePoint(1000L, 52000.0)))

        val points = viewModel.waitForPoints(52000.0)
        assertEquals(52000.0, points.last().price)
        viewModel.onCleared()
    }

    @Test
    fun `out of order trade is ignored`() = runTest {
        val fakeTradeUseCase = FakeObserveTradePricesUseCase()
        val viewModel = ChartScreenViewModel(
            getHistoricalPricesUseCase = FakeGetHistoricalPricesUseCase(prices = listOf(
                TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
            )),
            controllerFactory = { g, historical, scope ->
                ChartDataController(
                    observeTradePricesUseCase = fakeTradeUseCase,
                    priceAccumulator = PriceAccumulatorImpl(g, historical),
                    symbol = "BTCUSDT",
                    scope = scope,
                )
            },
            symbol = "BTCUSDT",
        )

        viewModel.load(Granularity.M1)
        viewModel.awaitChartData()

        fakeTradeUseCase.emitted.send(TradePrice("BTCUSDT", PricePoint(100_000L, 52000.0)))
        viewModel.waitForPoints(52000.0)

        fakeTradeUseCase.emitted.send(TradePrice("BTCUSDT", PricePoint(30_000L, 51000.0)))

        val points = waitForPriceStillVisible(viewModel, 52000.0)
        assertEquals(52000.0, points.last().price)
        viewModel.onCleared()
    }

    @Test
    fun `load called twice cancels previous job`() = runTest {
        val (viewModel, _, _) = createViewModel(historicalPrices = listOf(
            TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
        ))

        viewModel.load(Granularity.M1)
        viewModel.awaitChartData()

        viewModel.load(Granularity.M1)
        viewModel.awaitChartData()

        val state = viewModel.state.first()
        assertIs<Loadable.Loaded<*>>(state)
    }

    @Test
    fun `onCleared does not crash when no active job`() = runTest {
        val (viewModel, _, _) = createViewModel(historicalPointLimit = 200)
        viewModel.onCleared()
    }

    @Test
    fun `onCleared cancels active job`() = runTest {
        val (viewModel, _, _) = createViewModel(historicalPrices = listOf(
            TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
        ))

        viewModel.load(Granularity.M1)
        viewModel.awaitChartData()

        viewModel.onCleared()
    }

    @Test
    fun `live stream retries on failure and emits failed state`() = runTest {
        val (viewModel, _, _) = createViewModel(
            historicalPrices = listOf(TradePrice("BTCUSDT", PricePoint(0L, 50000.0))),
            tradeException = RuntimeException("Stream error"),
        )

        viewModel.load(Granularity.M1)
        viewModel.state.first { it !is Loadable.Loading }
        val loaded = viewModel.state.value as Loadable.Loaded
        val success = loaded.value as Fallible.Success
        val flow: Flow<Fallible<List<PricePoint>>> = success.value
        val fallible = flow.first { it is Fallible.Failed }
        assertIs<Fallible.Failed>(fallible)
    }

    @Test
    fun `emits failed state when historical fetch fails without starting stream`() = runTest {
        val (viewModel, _, _) = createViewModel(
            historicalPrices = emptyList(),
            historicalException = RuntimeException("History error"),
        )

        viewModel.load(Granularity.M1)
        val state = viewModel.state.first { it !is Loadable.Loading }
        assertIs<Loadable.Loaded<*>>(state)
        val loaded = state as Loadable.Loaded
        assertIs<Fallible.Failed>(loaded.value)
    }

    @Test
    fun `live stream stops retrying on CancellationException`() = runTest {
        val (viewModel, _, _) = createViewModel(
            historicalPrices = listOf(TradePrice("BTCUSDT", PricePoint(0L, 50000.0))),
            tradeException = CancellationException("Cancelled"),
        )

        viewModel.load(Granularity.M1)
        val chartData = viewModel.awaitChartData()

        assertEquals(50000.0, chartData.last().price)
    }
}
