package dyds.crypto.cecoin.presentation

import dyds.crypto.cecoin.domain.FakeTradePriceRepository
import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.presentation.chart.LiveChartViewModel
import dyds.crypto.cecoin.presentation.chart.model.Granularity
import dyds.crypto.cecoin.presentation.chart.util.foldTradePrice
import dyds.crypto.cecoin.utils.AppError
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LiveChartViewModelTest {

    @Test
    fun `loadPrices loads historical data and emits success`() = runTest {
        val historical = listOf(
            TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
            TradePrice("BTCUSDT", PricePoint(60_000L, 51000.0)),
        )
        val priceSource = FakeTradePriceRepository(historical = historical)
        val fakeBuilder = FakeChartModelBuilder()
        val viewModel = createViewModel(priceSource, fakeBuilder)

        viewModel.loadPrices()

        viewModel.asyncLoadState.first { it !is Loadable.Loading }
        assertEquals(51000.0, viewModel.lastPrice.first())
        assertTrue(fakeBuilder.called)
    }

    @Test
    fun `loadPrices emits failure when historical fetch fails`() = runTest {
        val priceSource = FakeTradePriceRepository(historicalException = RuntimeException("Network error"))
        val viewModel = createViewModel(priceSource)

        viewModel.loadPrices()

        val state = viewModel.asyncLoadState.first { it !is Loadable.Loading }
        assertIs<Loadable.Loaded<*>>(state)
        val fallible = (state as Loadable.Loaded).value
        assertIs<Fallible.Failed>(fallible)
        val error = fallible.error
        assertIs<AppError.GenericError>(error)
        assertTrue(error.userMessage.contains("Error al cargar gráfico"))
    }

    @Test
    fun `loadPrices sets stream state as Loading`() = runTest {
        val priceSource = FakeTradePriceRepository(historical = emptyList(), tradeFlow = MutableSharedFlow())
        val viewModel = createViewModel(priceSource)

        viewModel.loadPrices()

        viewModel.asyncLoadState.first { it !is Loadable.Loading }
        assertIs<Loadable.Loading>(viewModel.streamState.first())
    }

    @Test
    fun `setGranularity updates granularity`() = runTest {
        val priceSource = FakeTradePriceRepository(historical = emptyList())
        val viewModel = createViewModel(priceSource)

        viewModel.setGranularity(Granularity.M5)

        assertEquals(Granularity.M5, viewModel.granularity.first())
    }

    @Test
    fun `setGranularity same value does not reload`() = runTest {
        val priceSource = FakeTradePriceRepository(historical = listOf(
            TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
        ))
        val viewModel = createViewModel(priceSource)

        viewModel.loadPrices()
        viewModel.asyncLoadState.first { it !is Loadable.Loading }

        viewModel.setGranularity(Granularity.M1)

        assertIs<Loadable.Loaded<*>>(viewModel.asyncLoadState.first())
    }

    @Test
    fun `historical limit is passed through`() = runTest {
        val priceSource = FakeTradePriceRepository(historical = emptyList())
        val viewModel = LiveChartViewModel(
            getHistoricalPricesUseCase = GetHistoricalPricesUseCase(priceSource),
            observeTradePricesUseCase = ObserveTradePricesUseCase(priceSource),
            symbol = "BTCUSDT",
            historicalPointLimit = 50,
            chartModelBuilder = FakeChartModelBuilder(),
            retryDelayMs = 0,
        )

        viewModel.loadPrices()
        viewModel.asyncLoadState.first { it !is Loadable.Loading }

        assertEquals(50, priceSource.lastLimit)
    }

    @Test
    fun `live prices update lastPrice`() = runTest {
        val tradeFlow = MutableSharedFlow<TradePrice>()
        val priceSource = FakeTradePriceRepository(
            historical = listOf(TradePrice("BTCUSDT", PricePoint(0L, 50000.0))),
            tradeFlow = tradeFlow,
        )
        val fakeBuilder = FakeChartModelBuilder()
        val viewModel = createViewModel(priceSource, fakeBuilder)

        viewModel.loadPrices()
        viewModel.asyncLoadState.first { it !is Loadable.Loading }

        tradeFlow.emit(TradePrice("BTCUSDT", PricePoint(1000L, 52000.0)))

        assertEquals(52000.0, viewModel.lastPrice.first { it == 52000.0 })
        assertTrue(fakeBuilder.called)
    }

    @Test
    fun `out of order trade is ignored`() = runTest {
        val tradeFlow = MutableSharedFlow<TradePrice>()
        val priceSource = FakeTradePriceRepository(
            historical = listOf(TradePrice("BTCUSDT", PricePoint(0L, 50000.0))),
            tradeFlow = tradeFlow,
        )
        val fakeBuilder = FakeChartModelBuilder()
        val viewModel = createViewModel(priceSource, fakeBuilder)

        viewModel.loadPrices()
        viewModel.asyncLoadState.first { it !is Loadable.Loading }

        tradeFlow.emit(TradePrice("BTCUSDT", PricePoint(100_000L, 52000.0)))

        viewModel.lastPrice.first { it == 52000.0 }

        tradeFlow.emit(TradePrice("BTCUSDT", PricePoint(30_000L, 51000.0)))

        assertEquals(52000.0, viewModel.lastPrice.first())
    }

    @Test
    fun `loadPrices called twice cancels previous job and skips historical when points exist`() = runTest {
        val historical = listOf(
            TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
            TradePrice("BTCUSDT", PricePoint(60_000L, 51000.0)),
        )
        val priceSource = FakeTradePriceRepository(historical = historical)
        val fakeBuilder = FakeChartModelBuilder()
        val viewModel = createViewModel(priceSource, fakeBuilder)

        viewModel.loadPrices()
        viewModel.asyncLoadState.first { it !is Loadable.Loading }

        viewModel.loadPrices()
        viewModel.asyncLoadState.first { it !is Loadable.Loading }

        assertIs<Loadable.Loaded<*>>(viewModel.asyncLoadState.first())
    }

    @Test
    fun `onCleared does not crash when no active job`() = runTest {
        val priceSource = FakeTradePriceRepository()
        val viewModel = LiveChartViewModel(
            getHistoricalPricesUseCase = GetHistoricalPricesUseCase(priceSource),
            observeTradePricesUseCase = ObserveTradePricesUseCase(priceSource),
            symbol = "BTCUSDT",
            historicalPointLimit = 200,
            chartModelBuilder = FakeChartModelBuilder(),
            retryDelayMs = 0,
        )

        viewModel.onCleared()

        val state = viewModel.asyncLoadState.first()
        assertIs<Loadable.Loading>(state)
    }

    @Test
    fun `onCleared cancels active job`() = runTest {
        val priceSource = FakeTradePriceRepository(historical = listOf(
            TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
        ))
        val viewModel = LiveChartViewModel(
            getHistoricalPricesUseCase = GetHistoricalPricesUseCase(priceSource),
            observeTradePricesUseCase = ObserveTradePricesUseCase(priceSource),
            symbol = "BTCUSDT",
            historicalPointLimit = 200,
            chartModelBuilder = FakeChartModelBuilder(),
            retryDelayMs = 0,
        )

        viewModel.loadPrices()
        viewModel.asyncLoadState.first { it !is Loadable.Loading }

        viewModel.onCleared()

        assertIs<Loadable.Loaded<*>>(viewModel.asyncLoadState.first())
    }

    @Test
    fun `live stream retries on failure and emits error state`() = runTest {
        val tradeFlow = flow<Nothing> { throw RuntimeException("Stream error") }
        val priceSource = FakeTradePriceRepository(
            historical = listOf(TradePrice("BTCUSDT", PricePoint(0L, 50000.0))),
            tradeFlow = tradeFlow,
        )
        val viewModel = createViewModel(priceSource)

        viewModel.loadPrices()

        val state = viewModel.streamState.first { it !is Loadable.Loading }
        assertIs<Loadable.Loaded<*>>(state)
        val fallible = (state as Loadable.Loaded).value
        assertIs<Fallible.Failed>(fallible)
        val error = fallible.error as AppError.GenericError
        assertTrue(error.userMessage.contains("Error en transmisión en vivo"))
    }

    @Test
    fun `live stream processes trade when historical fetch fails and points are empty`() = runTest {
        val tradeFlow = MutableSharedFlow<TradePrice>()
        val priceSource = FakeTradePriceRepository(
            historical = emptyList(),
            historicalException = RuntimeException("History error"),
            tradeFlow = tradeFlow,
        )
        val viewModel = createViewModel(priceSource)

        viewModel.loadPrices()
        viewModel.asyncLoadState.first { it !is Loadable.Loading }

        tradeFlow.emit(TradePrice("BTCUSDT", PricePoint(100_000L, 52000.0)))
        viewModel.lastPrice.first { it == 52000.0 }

        assertIs<Loadable.Loaded<*>>(viewModel.asyncLoadState.first())
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

        viewModel.loadPrices()

        assertIs<Loadable.Loading>(viewModel.streamState.first())
    }

    @Test
    fun `foldTradePrice ignores out of order trade`() = runTest {
        val points = mutableListOf(PricePoint(60_000L, 52000.0))
        val trade = TradePrice("BTCUSDT", PricePoint(30_000L, 51000.0))
        points.foldTradePrice(trade, Granularity.M1)

        assertEquals(1, points.size)
        assertEquals(52000.0, points[0].price)
    }

    private fun createViewModel(
        priceSource: FakeTradePriceRepository = FakeTradePriceRepository(),
        fakeBuilder: FakeChartModelBuilder = FakeChartModelBuilder(),
    ): LiveChartViewModel {
        return LiveChartViewModel(
            getHistoricalPricesUseCase = GetHistoricalPricesUseCase(priceSource),
            observeTradePricesUseCase = ObserveTradePricesUseCase(priceSource),
            symbol = "BTCUSDT",
            historicalPointLimit = 200,
            chartModelBuilder = fakeBuilder,
            retryDelayMs = 0,
        )
    }
}
