package dyds.crypto.cecoin.presentation

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.domain.model.TradePrice
import dyds.crypto.cecoin.domain.repository.TradePriceRepository
import dyds.crypto.cecoin.domain.usecase.GetHistoricalPricesUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveTradePricesUseCase
import dyds.crypto.cecoin.presentation.chart.LiveChartViewModel
import dyds.crypto.cecoin.presentation.chart.model.Granularity
import dyds.crypto.cecoin.presentation.chart.util.ChartModelBuilder
import dyds.crypto.cecoin.utils.AppError
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LiveChartViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadPrices loads historical data and emits success`() = runBlocking {
        val historical = listOf(
            TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
            TradePrice("BTCUSDT", PricePoint(60_000L, 51000.0)),
        )
        val priceSource = FakeTradePriceRepo2(historical = historical)
        val fakeBuilder = FakeChartModelBuilder()
        val viewModel = createViewModel(priceSource, fakeBuilder)

        viewModel.loadPrices()

        viewModel.asyncLoadState.first { it !is Loadable.Loading }
        assertEquals(51000.0, viewModel.lastPrice.first())
        assertTrue(fakeBuilder.called)
    }

    @Test
    fun `loadPrices emits failure when historical fetch fails`() = runBlocking {
        val priceSource = FakeTradePriceRepo2(historicalException = RuntimeException("Network error"))
        val viewModel = createViewModel(priceSource)

        viewModel.loadPrices()

        val state = viewModel.asyncLoadState.first()
        assertIs<Loadable.Loaded<*>>(state)
        val fallible = (state as Loadable.Loaded).value
        assertIs<Fallible.Failed>(fallible)
        val error = (fallible as Fallible.Failed).error
        assertIs<AppError.GenericError>(error)
        assertTrue(error.userMessage.contains("Failed to load chart"))
    }

    @Test
    fun `loadPrices sets stream state as Loading`() = runBlocking {
        val priceSource = FakeTradePriceRepo2(historical = emptyList(), tradeFlow = MutableSharedFlow())
        val viewModel = createViewModel(priceSource)

        viewModel.loadPrices()

        viewModel.asyncLoadState.first { it !is Loadable.Loading }
        assertIs<Loadable.Loading>(viewModel.streamState.first())
        Unit
    }

    @Test
    fun `setGranularity updates granularity`() = runBlocking {
        val priceSource = FakeTradePriceRepo2(historical = emptyList())
        val viewModel = createViewModel(priceSource)

        viewModel.setGranularity(Granularity.M5)

        assertEquals(Granularity.M5, viewModel.granularity.first())
    }

    @Test
    fun `setGranularity same value does not reload`() = runBlocking {
        val priceSource = FakeTradePriceRepo2(historical = listOf(
            TradePrice("BTCUSDT", PricePoint(0L, 50000.0)),
        ))
        val viewModel = createViewModel(priceSource)

        viewModel.loadPrices()
        viewModel.asyncLoadState.first { it !is Loadable.Loading }

        viewModel.setGranularity(Granularity.M1)

        assertIs<Loadable.Loaded<*>>(viewModel.asyncLoadState.first())
        Unit
    }

    @Test
    fun `historical limit is passed through`() = runBlocking {
        val priceSource = FakeTradePriceRepo2(historical = emptyList())
        val viewModel = LiveChartViewModel(
            getHistoricalPricesUseCase = GetHistoricalPricesUseCase(priceSource),
            observeTradePricesUseCase = ObserveTradePricesUseCase(priceSource),
            symbol = "BTCUSDT",
            historicalPointLimit = 50,
            chartModelBuilder = FakeChartModelBuilder(),
        )

        viewModel.loadPrices()

        assertEquals(50, priceSource.lastLimit)
    }

    @Test
    fun `live prices update lastPrice`() = runBlocking {
        val tradeFlow = MutableSharedFlow<TradePrice>()
        val priceSource = FakeTradePriceRepo2(
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

    private fun createViewModel(
        priceSource: FakeTradePriceRepo2 = FakeTradePriceRepo2(),
        fakeBuilder: FakeChartModelBuilder = FakeChartModelBuilder(),
    ): LiveChartViewModel {
        return LiveChartViewModel(
            getHistoricalPricesUseCase = GetHistoricalPricesUseCase(priceSource),
            observeTradePricesUseCase = ObserveTradePricesUseCase(priceSource),
            symbol = "BTCUSDT",
            historicalPointLimit = 200,
            chartModelBuilder = fakeBuilder,
        )
    }
}

internal class FakeTradePriceRepo2(
    private val historical: List<TradePrice> = emptyList(),
    private val historicalException: Throwable? = null,
    private val tradeFlow: Flow<TradePrice> = emptyFlow(),
) : TradePriceRepository {
    var lastSymbol: String = ""
    var lastInterval: String = ""
    var lastLimit: Int = 0

    override suspend fun getHistoricalPrices(
        symbol: String, interval: String, limit: Int,
    ): List<TradePrice> {
        lastSymbol = symbol
        lastInterval = interval
        lastLimit = limit
        historicalException?.let { throw it }
        return historical
    }

    override fun observeTradePrices(symbol: String): Flow<TradePrice> {
        lastSymbol = symbol
        return tradeFlow
    }
}

internal class FakeChartModelBuilder : ChartModelBuilder {
    var called = false
    var lastPoints: List<PricePoint> = emptyList()
    var lastProducer: CartesianChartModelProducer? = null

    override suspend fun buildModel(
        points: List<PricePoint>,
        modelProducer: CartesianChartModelProducer,
    ) {
        called = true
        lastPoints = points
        lastProducer = modelProducer
    }
}
