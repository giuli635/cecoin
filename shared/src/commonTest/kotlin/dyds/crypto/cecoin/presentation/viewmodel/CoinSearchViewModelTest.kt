package dyds.crypto.cecoin.presentation.viewmodel

import dyds.crypto.cecoin.domain.FakeCryptoSymbolRepository
import dyds.crypto.cecoin.domain.FakeFavoriteRepository
import dyds.crypto.cecoin.domain.model.CryptoSymbol
import dyds.crypto.cecoin.domain.repository.CryptoSymbolRepository
import dyds.crypto.cecoin.domain.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveFavoritesUseCase
import dyds.crypto.cecoin.domain.usecase.ToggleFavoriteUseCase
import dyds.crypto.cecoin.presentation.search.CoinSearchViewModel
import dyds.crypto.cecoin.presentation.search.FilterMode
import dyds.crypto.cecoin.utils.AppError
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CoinSearchViewModelTest {

    @Test
    fun `init loads symbols and emits success`() = runTest {
        val repo = FakeCryptoSymbolRepository(listOf(
            CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"),
            CryptoSymbol("ETHUSDT", "ETH", "USDT", "TRADING"),
        ))
        val viewModel = createViewModel(symbolRepo = repo)

        val result = viewModel.filteredCoins.first { it !is Loadable.Loading }

        assertIs<Loadable.Loaded<*>>(result)
        val fallible = (result as Loadable.Loaded).value
        assertIs<Fallible.Success<*>>(fallible)
        @Suppress("UNCHECKED_CAST")
        assertEquals(
            listOf("BTCUSDT", "ETHUSDT"),
            ((fallible as Fallible.Success<*>).value as List<String>).sorted(),
        )
    }

    @Test
    fun `loadSymbols emits failed when repository throws`() = runTest {
        val repo = FakeCryptoSymbolRepository(exception = RuntimeException("API error"))
        val viewModel = createViewModel(symbolRepo = repo)

        val result = viewModel.filteredCoins.first { it !is Loadable.Loading }

        assertIs<Loadable.Loaded<*>>(result)
        val fallible = (result as Loadable.Loaded).value
        assertIs<Fallible.Failed>(fallible)
        val error = (fallible as Fallible.Failed).error
        assertIs<AppError.GenericError>(error)
        assertTrue(error.userMessage.contains("Error al cargar símbolos"))
    }

    @Test
    fun `onSearchQueryChange updates search query in uiState`() = runTest {
        val viewModel = createViewModel()

        viewModel.onSearchQueryChange("BTC")

        val state = viewModel.uiState.first()
        assertEquals("BTC", state.searchQuery)
    }

    @Test
    fun `onSearchQueryChange filters coins`() = runTest {
        val repo = FakeCryptoSymbolRepository(listOf(
            CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"),
            CryptoSymbol("ETHUSDT", "ETH", "USDT", "TRADING"),
            CryptoSymbol("BNBUSDT", "BNB", "USDT", "TRADING"),
        ))
        val viewModel = createViewModel(symbolRepo = repo)

        viewModel.filteredCoins.first { it !is Loadable.Loading }

        viewModel.onSearchQueryChange("btc")

        val result = viewModel.filteredCoins.first { it is Loadable.Loaded && it.value is Fallible.Success && (it.value as Fallible.Success<*>).value == listOf("BTCUSDT") }
        val symbols = extractSymbols(result)
        assertEquals(listOf("BTCUSDT"), symbols)
    }

    @Test
    fun `setFilterMode to FAVORITES shows only favorites`() = runTest {
        val repo = FakeCryptoSymbolRepository(listOf(
            CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"),
            CryptoSymbol("ETHUSDT", "ETH", "USDT", "TRADING"),
        ))
        val favRepo = createFakeFavoriteRepository(initialFavorites = setOf("ETHUSDT"))
        val viewModel = createViewModel(symbolRepo = repo, favRepo = favRepo)

        viewModel.filteredCoins.first { it !is Loadable.Loading }

        viewModel.setFilterMode(FilterMode.FAVORITES)

        val result = viewModel.filteredCoins.first { it is Loadable.Loaded && it.value is Fallible.Success && (it.value as Fallible.Success<*>).value == listOf("ETHUSDT") }
        val symbols = extractSymbols(result)
        assertEquals(listOf("ETHUSDT"), symbols)
    }

    @Test
    fun `setFilterMode to ALL shows all symbols`() = runTest {
        val repo = FakeCryptoSymbolRepository(listOf(
            CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"),
            CryptoSymbol("ETHUSDT", "ETH", "USDT", "TRADING"),
        ))
        val viewModel = createViewModel(symbolRepo = repo)

        viewModel.filteredCoins.first { it !is Loadable.Loading }
        viewModel.setFilterMode(FilterMode.FAVORITES)
        viewModel.filteredCoins.first { it is Loadable.Loaded && it.value is Fallible.Success && (it.value as Fallible.Success<*>).value == emptyList<String>() }
        viewModel.setFilterMode(FilterMode.ALL)

        val result = viewModel.filteredCoins.first { it is Loadable.Loaded && it.value is Fallible.Success && (it.value as Fallible.Success<*>).value == listOf("BTCUSDT", "ETHUSDT") }
        val symbols = extractSymbols(result)
        assertEquals(2, symbols.size)
    }

    @Test
    fun `toggleFavorite delegates to use case`() = runTest {
        val favRepo = createFakeFavoriteRepository()
        val viewModel = createViewModel(favRepo = favRepo)

        viewModel.toggleFavorite("BTCUSDT")

        assertTrue("BTCUSDT" in viewModel.favorites.first { "BTCUSDT" in it })
    }

    @Test
    fun `onCancelLoadSymbols cancels active job`() = runTest {
        val repo = FakeCryptoSymbolRepository(
            symbols = listOf(CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING")),
        )
        val viewModel = createViewModel(symbolRepo = repo)

        viewModel.filteredCoins.first { it !is Loadable.Loading }
        viewModel.onCancelLoadSymbols()

        val state = viewModel.uiState.first()
        assertEquals("", state.searchQuery)
    }

    @Test
    fun `onCancelLoadSymbols does nothing when no active job`() = runTest {
        val viewModel = createViewModel()

        viewModel.onCancelLoadSymbols()

        val state = viewModel.uiState.first()
        assertEquals("", state.searchQuery)
    }

    @Test
    fun `filteredCoins emits Loading initially before data is loaded`() = runTest {
        val symbolsDeferred = CompletableDeferred<List<CryptoSymbol>>()
        val repo = object : CryptoSymbolRepository {
            override suspend fun getAvailableSymbols(): List<CryptoSymbol> {
                return symbolsDeferred.await()
            }
        }
        val favRepo = createFakeFavoriteRepository()
        val viewModel = CoinSearchViewModel(
            getAvailableSymbolsUseCase = GetAvailableSymbolsUseCase(repo),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(favRepo),
            observeFavoritesUseCase = ObserveFavoritesUseCase(favRepo),
        )

        val initial = viewModel.filteredCoins.first()
        assertIs<Loadable.Loading>(initial)

        symbolsDeferred.complete(listOf(CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING")))

        val loaded = viewModel.filteredCoins.first { it !is Loadable.Loading }
        assertIs<Loadable.Loaded<*>>(loaded)
    }

    @Test
    fun `onCancelLoadSymbols handles both null and non-null job`() = runTest {
        val repo = FakeCryptoSymbolRepository(symbols = listOf(
            CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"),
        ))
        val viewModel = createViewModel(symbolRepo = repo)

        viewModel.filteredCoins.first { it !is Loadable.Loading }

        viewModel.onCancelLoadSymbols()
        viewModel.onCancelLoadSymbols()
    }

    @Test
    fun `retryLoadSymbols reloads after failure`() = runTest {
        val repo = FakeCryptoSymbolRepository(exception = RuntimeException("fail"))
        val viewModel = createViewModel(symbolRepo = repo)

        viewModel.filteredCoins.first { it !is Loadable.Loading }

        repo.exception = null
        repo.symbols = listOf(CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"))

        viewModel.retryLoadSymbols()

        val result = viewModel.filteredCoins.first { it is Loadable.Loaded && it.value is Fallible.Success && (it.value as Fallible.Success<*>).value == listOf("BTCUSDT") }
        val symbols = extractSymbols(result)
        assertEquals(listOf("BTCUSDT"), symbols)
    }

    private fun createViewModel(
        symbolRepo: FakeCryptoSymbolRepository = FakeCryptoSymbolRepository(),
        favRepo: FakeFavoriteRepository = createFakeFavoriteRepository(),
    ): CoinSearchViewModel {
        val getSymbols = GetAvailableSymbolsUseCase(symbolRepo)
        val toggleFav = ToggleFavoriteUseCase(favRepo)
        val observeFav = ObserveFavoritesUseCase(favRepo)
        return CoinSearchViewModel(getSymbols, toggleFav, observeFav)
    }

    private fun createFakeFavoriteRepository(initialFavorites: Set<String> = emptySet()): FakeFavoriteRepository {
        return FakeFavoriteRepository(initialFavorites = initialFavorites)
    }

    private fun extractSymbols(result: Loadable<*>): List<String> {
        val loaded = result as Loadable.Loaded
        val success = loaded.value as Fallible.Success<*>
        @Suppress("UNCHECKED_CAST")
        return (success.value as List<String>).sorted()
    }
}
