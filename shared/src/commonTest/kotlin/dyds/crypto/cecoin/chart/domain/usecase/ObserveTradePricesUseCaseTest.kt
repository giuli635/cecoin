package dyds.crypto.cecoin.chart.domain.usecase

import dyds.crypto.cecoin.chart.domain.FakeTradePriceRepository
import dyds.crypto.cecoin.chart.domain.model.PricePoint
import dyds.crypto.cecoin.chart.domain.model.TradePrice
import dyds.crypto.cecoin.core.utils.error.fakeErrorClassifier
import dyds.crypto.cecoin.core.utils.state.Fallible
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

class ObserveTradePricesUseCaseTest {
    private val classifier = fakeErrorClassifier()

    @Test
    fun `invoke returns flow from repository`() = runTest {
        val expected = TradePrice("BTCUSDT", PricePoint(1000L, 50000.0))
        val repo = FakeTradePriceRepository(tradeFlow = flowOf(expected))
        val useCase = ObserveTradePricesUseCaseImpl(repo, classifier)

        val result = useCase("BTCUSDT")

        val fallible = result.first()
        val success = fallible as Fallible.Success
        assertEquals(expected, success.value)
    }

    @Test
    fun `invoke emits multiple trades`() = runTest {
        val t1 = TradePrice("BTCUSDT", PricePoint(1000L, 50000.0))
        val t2 = TradePrice("BTCUSDT", PricePoint(2000L, 51000.0))
        val repo = FakeTradePriceRepository(tradeFlow = flow { emit(t1); emit(t2) })
        val useCase = ObserveTradePricesUseCaseImpl(repo, classifier)

        val results = useCase("BTCUSDT").take(2).toList()

        assertEquals(2, results.size)
        val s1 = assertIs<Fallible.Success<TradePrice>>(results[0])
        assertEquals(50000.0, s1.value.price)
        val s2 = assertIs<Fallible.Success<TradePrice>>(results[1])
        assertEquals(51000.0, s2.value.price)
    }

    @Test
    fun `invoke exhausts retries and stops`() = runTest {
        val repo = FakeTradePriceRepository(tradeFlow = flow { throw RuntimeException("stream fail") })
        val useCase = ObserveTradePricesUseCaseImpl(repo, classifier, retryDelayMs = 1L, maxRetries = 3)

        val results = useCase("BTCUSDT").toList()

        assertEquals(4, results.size)
        results.forEach { assertIs<Fallible.Failed>(it) }
    }

    @Test
    fun `invoke emits nothing when repository flow never emits`() = runTest {
        val repo = FakeTradePriceRepository(tradeFlow = flow { delay(Long.MAX_VALUE) })
        val useCase = ObserveTradePricesUseCaseImpl(repo, classifier)

        val result = withTimeoutOrNull(100.milliseconds) {
            useCase("BTCUSDT").firstOrNull()
        }

        assertNull(result)
    }

    @Test
    fun `invoke handles many emissions without accumulation issues`() = runTest {
        val trades = (1..100).map { TradePrice("BTCUSDT", PricePoint(it.toLong(), 50000.0 + it)) }
        val repo = FakeTradePriceRepository(tradeFlow = flow { trades.forEach { emit(it) } })
        val useCase = ObserveTradePricesUseCaseImpl(repo, classifier)

        val results = useCase("BTCUSDT").take(100).toList()

        assertEquals(100, results.size)
        results.forEach { assertIs<Fallible.Success<TradePrice>>(it) }
    }
}
