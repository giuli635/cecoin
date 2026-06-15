package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.FakeCryptoSymbolRepository
import dyds.crypto.cecoin.domain.FakeFavoriteRepository
import dyds.crypto.cecoin.domain.FakeTradePriceRepository
import dyds.crypto.cecoin.domain.model.CryptoSymbol
import dyds.crypto.cecoin.domain.model.PricePoint
import dyds.crypto.cecoin.domain.model.TradePrice
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAvailableSymbolsUseCaseTest {
    @Test
    fun `invoke returns symbols from repository`() = runTest {
        val expected = listOf(CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"))
        val repo = FakeCryptoSymbolRepository(expected)
        val useCase = GetAvailableSymbolsUseCase(repo)

        val result = useCase()

        assertEquals(expected, result)
    }

    @Test
    fun `invoke returns empty list when repository returns empty`() = runTest {
        val repo = FakeCryptoSymbolRepository(emptyList())
        val useCase = GetAvailableSymbolsUseCase(repo)

        val result = useCase()

        assertEquals(0, result.size)
    }
}

class GetHistoricalPricesUseCaseTest {
    @Test
    fun `invoke delegates to repository with correct params`() = runTest {
        val expected = listOf(TradePrice("BTCUSDT", PricePoint(1000L, 50000.0)))
        val repo = FakeTradePriceRepository(historical = expected)
        val useCase = GetHistoricalPricesUseCase(repo)

        val result = useCase("BTCUSDT", "5m", 100)

        assertEquals(expected, result)
    }

    @Test
    fun `invoke uses default interval and limit`() = runTest {
        val expected = listOf(TradePrice("ETHUSDT", PricePoint(2000L, 3000.0)))
        val repo = FakeTradePriceRepository(historical = expected)
        val useCase = GetHistoricalPricesUseCase(repo)

        val result = useCase("ETHUSDT")

        assertEquals(expected, result)
    }
}

class ObserveTradePricesUseCaseTest {
    @Test
    fun `invoke returns flow from repository`() = runTest {
        val expected = TradePrice("BTCUSDT", PricePoint(1000L, 50000.0))
        val repo = FakeTradePriceRepository(tradeFlow = flowOf(expected))
        val useCase = ObserveTradePricesUseCase(repo)

        val result = useCase("BTCUSDT")

        assertEquals(expected, result.first())
    }
}

class ObserveFavoritesUseCaseTest {
    @Test
    fun `invoke returns favorites flow from repository`() = runTest {
        val expected = setOf("BTCUSDT", "ETHUSDT")
        val repo = FakeFavoriteRepository(initialFavorites = expected)
        val useCase = ObserveFavoritesUseCase(repo)

        val result = useCase()

        assertEquals(expected, result.first())
    }
}

class ToggleFavoriteUseCaseTest {
    @Test
    fun `invoke delegates toggle to repository`() = runTest {
        val repo = FakeFavoriteRepository()
        val useCase = ToggleFavoriteUseCase(repo)

        useCase("BTCUSDT")

        assertEquals("BTCUSDT", repo.toggledSymbol)
    }
}


