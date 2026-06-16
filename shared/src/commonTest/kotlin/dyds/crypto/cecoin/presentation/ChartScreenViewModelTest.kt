package dyds.crypto.cecoin.presentation

import dyds.crypto.cecoin.domain.FakeTradePriceRepository
import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.presentation.chart.ChartDataController
import dyds.crypto.cecoin.presentation.chart.ChartScreenViewModel
import dyds.crypto.cecoin.presentation.chart.model.ChartData
import dyds.crypto.cecoin.presentation.chart.model.Granularity
import dyds.crypto.cecoin.presentation.chart.util.foldTradePrice
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ChartScreenViewModelTest {

    private suspend fun ChartScreenViewModel.awaitChartData(): ChartData {
        val state = state.first { it !is Loadable.Loading }
        val loaded = state as Loadable.Loaded
        val success = loaded.value as Fallible.Success
        val flow: Flow<Fallible<ChartData>> = success.value
        val fallible = flow.first { it is Fallible.Success }
        val successValue = fallible as Fallible.Success
        return successValue.value
    }

    private suspend fun ChartScreenViewModel.waitForPoints(price: Double): List<PricePoint> {
        val state = state.first()
        val loaded = state as Loadable.Loaded
        val success = loaded.value as Fallible.Success
        val flow: Flow<Fallible<ChartData>> = success.value
        val emission = flow.first { it is Fallible.Success && it.value.any { p -> p.price == price } }
        val successValue = emission as Fallible.Success
        return successValue.value
    }

    @Test
    fun `loads historical data and emits success`() = runTest {
        val historical = listOf(
            TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
            TradePrice("BTCUSDT", PricePoint(60_000L, 51000.0)),
        )
        val priceSource = FakeTradePriceRepository(historical = historical)
        val viewModel = createViewModel(priceSource)

        viewModel.load(Granularity.M1)
        val chartData = viewModel.awaitChartData()

        assertTrue(chartData.isNotEmpty())
        assertEquals(51000.0, chartData.last().price)
    }

    @Test
    fun `emits failure when historical fetch fails`() = runTest {
        val priceSource = FakeTradePriceRepository(historicalException = RuntimeException("Network error"))
        val viewModel = createViewModel(priceSource)

        viewModel.load(Granularity.M1)
        val state = viewModel.state.first { it !is Loadable.Loading }
        assertIs<Loadable.Loaded<*>>(state)
        val loaded = state as Loadable.Loaded
        assertIs<Fallible.Failed>(loaded.value)
    }

    @Test
    fun `sets stream state as Loading`() = runTest {
        val priceSource = FakeTradePriceRepository(historical = emptyList(), tradeFlow = MutableSharedFlow())
        val viewModel = createViewModel(priceSource)

        viewModel.load(Granularity.M1)
        val chartData = viewModel.awaitChartData()

        assertTrue(chartData.isEmpty())
    }

    @Test
    fun `granularity change triggers new load`() = runTest {
        val priceSource = FakeTradePriceRepository(historical = emptyList())
        val viewModel = createViewModel(priceSource)

        viewModel.load(Granularity.M1)
        viewModel.awaitChartData()

        viewModel.load(Granularity.M5)
        val data = viewModel.awaitChartData()
        assertTrue(data.isEmpty())
    }

    @Test
    fun `same granularity value does not reload`() = runTest {
        val priceSource = FakeTradePriceRepository(historical = listOf(
            TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
        ))
        val viewModel = createViewModel(priceSource)

        viewModel.load(Granularity.M1)
        viewModel.awaitChartData()

        viewModel.load(Granularity.M1)
        val state = viewModel.state.first { it !is Loadable.Loading }
        assertIs<Loadable.Loaded<*>>(state)
    }

    @Test
    fun `historical limit is passed through`() = runTest {
        val priceSource = FakeTradePriceRepository(historical = emptyList())
        val viewModel = createViewModel(
            priceSource = priceSource,
            historicalPointLimit = 50,
        )

        viewModel.load(Granularity.M1)
        viewModel.awaitChartData()

        assertEquals(50, priceSource.lastLimit)
    }

    @Test
    fun `live prices update lastPrice`() = runTest {
        val tradeFlow = MutableSharedFlow<TradePrice>()
        val priceSource = FakeTradePriceRepository(
            historical = listOf(TradePrice("BTCUSDT", PricePoint(0L, 50000.0))),
            tradeFlow = tradeFlow,
        )
        val viewModel = createViewModel(priceSource)

        viewModel.load(Granularity.M1)
        val initialData = viewModel.awaitChartData()
        assertEquals(50000.0, initialData.last().price)

        tradeFlow.emit(TradePrice("BTCUSDT", PricePoint(1000L, 52000.0)))

        val points = viewModel.waitForPoints(52000.0)
        assertEquals(52000.0, points.last().price)
        viewModel.onCleared()
    }

    @Test
    fun `out of order trade is ignored`() = runTest {
        val tradeFlow = MutableSharedFlow<TradePrice>()
        val priceSource = FakeTradePriceRepository(
            historical = listOf(TradePrice("BTCUSDT", PricePoint(0L, 50000.0))),
            tradeFlow = tradeFlow,
        )
        val viewModel = createViewModel(priceSource)

        viewModel.load(Granularity.M1)
        viewModel.awaitChartData()

        tradeFlow.emit(TradePrice("BTCUSDT", PricePoint(100_000L, 52000.0)))
        viewModel.waitForPoints(52000.0)

        tradeFlow.emit(TradePrice("BTCUSDT", PricePoint(30_000L, 51000.0)))

        val points = waitForPriceStillVisible(viewModel, 52000.0)
        assertEquals(52000.0, points.last().price)
        viewModel.onCleared()
    }

    @Test
    fun `load called twice cancels previous job`() = runTest {
        val priceSource = FakeTradePriceRepository(historical = listOf(
            TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
        ))
        val viewModel = createViewModel(priceSource)

        viewModel.load(Granularity.M1)
        viewModel.awaitChartData()

        viewModel.load(Granularity.M1)
        viewModel.awaitChartData()

        val state = viewModel.state.first()
        assertIs<Loadable.Loaded<*>>(state)
    }

    @Test
    fun `onCleared does not crash when no active job`() = runTest {
        val priceSource = FakeTradePriceRepository()
        val viewModel = createViewModel(
            priceSource = priceSource,
            historicalPointLimit = 200,
        )

        viewModel.onCleared()
    }

    @Test
    fun `onCleared cancels active job`() = runTest {
        val priceSource = FakeTradePriceRepository(historical = listOf(
            TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
        ))
        val viewModel = createViewModel(
            priceSource = priceSource,
            historicalPointLimit = 200,
        )

        viewModel.load(Granularity.M1)
        viewModel.awaitChartData()

        viewModel.onCleared()
    }

    @Test
    fun `live stream retries on failure and emits failed state`() = runTest {
        val tradeFlow = flow<Nothing> { throw RuntimeException("Stream error") }
        val priceSource = FakeTradePriceRepository(
            historical = listOf(TradePrice("BTCUSDT", PricePoint(0L, 50000.0))),
            tradeFlow = tradeFlow,
        )
        val viewModel = createViewModel(priceSource)

        viewModel.load(Granularity.M1)
        viewModel.state.first { it !is Loadable.Loading }
        val loaded = viewModel.state.value as Loadable.Loaded
        val success = loaded.value as Fallible.Success
        val flow: Flow<Fallible<ChartData>> = success.value
        val fallible = flow.first { it is Fallible.Failed }
        assertIs<Fallible.Failed>(fallible)
    }

    @Test
    fun `emits failed state when historical fetch fails without starting stream`() = runTest {
        val priceSource = FakeTradePriceRepository(
            historical = emptyList(),
            historicalException = RuntimeException("History error"),
        )
        val viewModel = createViewModel(priceSource)

        viewModel.load(Granularity.M1)
        val state = viewModel.state.first { it !is Loadable.Loading }
        assertIs<Loadable.Loaded<*>>(state)
        val loaded = state as Loadable.Loaded
        assertIs<Fallible.Failed>(loaded.value)
    }

    @Test
    fun `foldTradePrice adds price when points is empty`() = runTest {
        val points = mutableListOf<PricePoint>()
        val trade = TradePrice("BTCUSDT", PricePoint(100_000L, 52000.0))
        points.foldTradePrice(trade, Granularity.M1)

        val expectedTimestamp = (100_000L / 60_000L) * 60_000L
        assertEquals(1, points.size)
        assertEquals(expectedTimestamp, points[0].timestamp)
        assertEquals(52000.0, points[0].price)
    }

    @Test
    fun `live stream stops retrying on CancellationException`() = runTest {
        val tradeFlow = flow<Nothing> { throw CancellationException("Cancelled") }
        val priceSource = FakeTradePriceRepository(
            historical = listOf(TradePrice("BTCUSDT", PricePoint(0L, 50000.0))),
            tradeFlow = tradeFlow,
        )
        val viewModel = createViewModel(priceSource)

        viewModel.load(Granularity.M1)
        val chartData = viewModel.awaitChartData()

        assertEquals(50000.0, chartData.last().price)
    }

    @Test
    fun `foldTradePrice ignores out of order trade`() = runTest {
        val points = mutableListOf(PricePoint(60_000L, 52000.0))
        val trade = TradePrice("BTCUSDT", PricePoint(30_000L, 51000.0))
        points.foldTradePrice(trade, Granularity.M1)

        assertEquals(1, points.size)
        assertEquals(52000.0, points[0].price)
    }

    private suspend fun waitForPriceStillVisible(viewModel: ChartScreenViewModel, price: Double): List<PricePoint> {
        val state = viewModel.state.first()
        val loaded = state as Loadable.Loaded
        val success = loaded.value as Fallible.Success
        val flow: Flow<Fallible<ChartData>> = success.value
        val emission = flow.first { it is Fallible.Success }
        val successValue = emission as Fallible.Success
        return successValue.value
    }

    private fun createViewModel(
        priceSource: FakeTradePriceRepository = FakeTradePriceRepository(),
        historicalPointLimit: Int = 200,
    ): ChartScreenViewModel {
        return ChartScreenViewModel(
            getHistoricalPricesUseCase = GetHistoricalPricesUseCase(priceSource),
            controllerFactory = { g ->
                ChartDataController(
                    observeTradePricesUseCase = ObserveTradePricesUseCase(priceSource),
                    symbol = "BTCUSDT",
                    retryDelayMs = 0,
                )
            },
            symbol = "BTCUSDT",
            historicalPointLimit = historicalPointLimit,
        )
    }
}
