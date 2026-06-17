package dyds.crypto.cecoin.chart.presentation

import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.domain.model.TradePrice
import dyds.crypto.cecoin.chart.domain.usecase.FakeGetHistoricalPricesUseCase
import dyds.crypto.cecoin.chart.presentation.model.Granularity
import dyds.crypto.cecoin.core.utils.state.Fallible
import dyds.crypto.cecoin.core.utils.state.Loadable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChartScreenViewModelTest {

    private suspend fun extractFlowSuccess(
        stateProvider: suspend () -> Loadable<*>,
        flowPredicate: (Fallible<List<PricePoint>>) -> Boolean = { it is Fallible.Success },
    ): List<PricePoint> {
        val state = stateProvider()
        val loaded = assertIs<Loadable.Loaded<Flow<Fallible<List<PricePoint>>>>>(state)
        val success = assertIs<Fallible.Success<Flow<Fallible<List<PricePoint>>>>>(loaded.value)
        val flow = success.value
        val emission = flow.first { flowPredicate(it) }
        return assertIs<Fallible.Success<List<PricePoint>>>(emission).value
    }

    private suspend fun ChartScreenViewModel.awaitChartData(): List<PricePoint> =
        extractFlowSuccess({ state.first { it !is Loadable.Loading } })

    private suspend fun ChartScreenViewModel.waitForPoints(price: Double): List<PricePoint> =
        extractFlowSuccess(
            stateProvider = { state.first() },
            flowPredicate = { it is Fallible.Success && it.value.any { p -> p.price == price } },
        )

    private suspend fun waitForPriceStillVisible(viewModel: ChartScreenViewModel): List<PricePoint> =
        extractFlowSuccess({ viewModel.state.first() })

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
            observeTradePricesUseCase = fakeTradeUseCase,
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
        viewModel.onCleared()
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
        viewModel.onCleared()
    }

    @Test
    fun `sets stream state as Loading`() = runTest {
        val (viewModel, _, _) = createViewModel()

        viewModel.load(Granularity.M1)
        val chartData = viewModel.awaitChartData()

        assertTrue(chartData.isEmpty())
        viewModel.onCleared()
    }

    @Test
    fun `granularity change triggers new load`() = runTest {
        val (viewModel, _, _) = createViewModel()

        viewModel.load(Granularity.M1)
        viewModel.awaitChartData()

        viewModel.load(Granularity.M5)
        val data = viewModel.awaitChartData()
        assertTrue(data.isEmpty())
        viewModel.onCleared()
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
        viewModel.onCleared()
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
        viewModel.onCleared()
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
        val fakeHistorical = FakeGetHistoricalPricesUseCase(prices = listOf(
            TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
        ))
        val viewModel = ChartScreenViewModel(
            getHistoricalPricesUseCase = fakeHistorical,
            observeTradePricesUseCase = fakeTradeUseCase,
            symbol = "BTCUSDT",
        )

        viewModel.load(Granularity.M1)
        viewModel.awaitChartData()

        fakeTradeUseCase.emitted.send(TradePrice("BTCUSDT", PricePoint(100_000L, 52000.0)))
        viewModel.waitForPoints(52000.0)

        fakeTradeUseCase.emitted.send(TradePrice("BTCUSDT", PricePoint(30_000L, 51000.0)))

        val points = waitForPriceStillVisible(viewModel)
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
        advanceUntilIdle()

        val state = viewModel.state.first()
        assertIs<Loadable.Loaded<*>>(state)
        viewModel.onCleared()
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
        val loaded = assertIs<Loadable.Loaded<*>>(viewModel.state.value)
        val success = assertIs<Fallible.Success<Flow<Fallible<List<PricePoint>>>>>(loaded.value)
        val fallible = success.value.first { it is Fallible.Failed }
        assertIs<Fallible.Failed>(fallible)
        viewModel.onCleared()
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
        viewModel.onCleared()
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
        viewModel.onCleared()
    }
}
