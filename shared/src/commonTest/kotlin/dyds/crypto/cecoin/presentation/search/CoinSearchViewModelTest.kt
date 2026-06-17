package dyds.crypto.cecoin.presentation.search

import dyds.crypto.cecoin.domain.model.CryptoSymbol
import dyds.crypto.cecoin.domain.usecase.FakeGetAvailableSymbolsUseCase
import dyds.crypto.cecoin.domain.usecase.FakeObserveFavoritesUseCase
import dyds.crypto.cecoin.domain.usecase.FakeToggleFavoriteUseCase
import dyds.crypto.cecoin.domain.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.utils.AppError
import dyds.crypto.cecoin.utils.ErrorClassifier
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CoinSearchViewModelTest {

    @Test
    fun `init loads symbols and emits success`() = runTest {
        val symbolsFake = FakeGetAvailableSymbolsUseCase(listOf(
            CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"),
            CryptoSymbol("ETHUSDT", "ETH", "USDT", "TRADING"),
        ))
        val viewModel = createViewModel(symbolsFake = symbolsFake)

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
    fun `loadSymbols emits failed when usecase throws`() = runTest {
        val symbolsFake = FakeGetAvailableSymbolsUseCase(exception = RuntimeException("API error"))
        val viewModel = createViewModel(symbolsFake = symbolsFake)

        val result = viewModel.filteredCoins.first { it !is Loadable.Loading }

        assertIs<Loadable.Loaded<*>>(result)
        val fallible = (result as Loadable.Loaded).value
        assertIs<Fallible.Failed>(fallible)
        val error = fallible.error
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
        val symbolsFake = FakeGetAvailableSymbolsUseCase(listOf(
            CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"),
            CryptoSymbol("ETHUSDT", "ETH", "USDT", "TRADING"),
            CryptoSymbol("BNBUSDT", "BNB", "USDT", "TRADING"),
        ))
        val viewModel = createViewModel(symbolsFake = symbolsFake)

        viewModel.filteredCoins.first { it !is Loadable.Loading }

        viewModel.onSearchQueryChange("btc")

        val result = viewModel.filteredCoins.first { it is Loadable.Loaded && it.value is Fallible.Success && (it.value as Fallible.Success<*>).value == listOf("BTCUSDT") }
        val symbols = extractSymbols(result)
        assertEquals(listOf("BTCUSDT"), symbols)
    }

    @Test
    fun `setFilterMode to FAVORITES shows only favorites`() = runTest {
        val symbolsFake = FakeGetAvailableSymbolsUseCase(listOf(
            CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"),
            CryptoSymbol("ETHUSDT", "ETH", "USDT", "TRADING"),
        ))
        val observeFake = FakeObserveFavoritesUseCase(initial = setOf("ETHUSDT"))
        val viewModel = createViewModel(symbolsFake = symbolsFake, observeFake = observeFake)

        viewModel.filteredCoins.first { it !is Loadable.Loading }
        viewModel.setFilterMode(FilterMode.FAVORITES)

        val result = viewModel.filteredCoins.first { it is Loadable.Loaded && it.value is Fallible.Success && (it.value as Fallible.Success<*>).value == listOf("ETHUSDT") }
        val symbols = extractSymbols(result)
        assertEquals(listOf("ETHUSDT"), symbols)
    }

    @Test
    fun `setFilterMode to ALL shows all symbols`() = runTest {
        val symbolsFake = FakeGetAvailableSymbolsUseCase(listOf(
            CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"),
            CryptoSymbol("ETHUSDT", "ETH", "USDT", "TRADING"),
        ))
        val viewModel = createViewModel(symbolsFake = symbolsFake)

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
        val sharedFavorites = MutableStateFlow(emptySet<String>())
        val toggleFake = FakeToggleFavoriteUseCase(favorites = sharedFavorites)
        val observeFake = FakeObserveFavoritesUseCase(flow = sharedFavorites)
        val viewModel = createViewModel(toggleFake = toggleFake, observeFake = observeFake)

        viewModel.toggleFavorite("BTCUSDT")

        assertTrue("BTCUSDT" in viewModel.favorites.first { "BTCUSDT" in it })
        assertEquals("BTCUSDT", toggleFake.lastToggled)
    }

    @Test
    fun `onCancelLoadSymbols cancels active job`() = runTest {
        val symbolsFake = FakeGetAvailableSymbolsUseCase(listOf(
            CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"),
        ))
        val viewModel = createViewModel(symbolsFake = symbolsFake)

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
        val getSymbols = object : GetAvailableSymbolsUseCase {
            override suspend fun invoke(): List<CryptoSymbol> = symbolsDeferred.await()
        }
        val viewModel = CoinSearchViewModel(
            getAvailableSymbolsUseCase = getSymbols,
            toggleFavoriteUseCase = FakeToggleFavoriteUseCase(),
            observeFavoritesUseCase = FakeObserveFavoritesUseCase(),
            errorClassifier = fakeClassifier(),
        )

        val initial = viewModel.filteredCoins.first()
        assertIs<Loadable.Loading>(initial)

        symbolsDeferred.complete(listOf(CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING")))

        val loaded = viewModel.filteredCoins.first { it !is Loadable.Loading }
        assertIs<Loadable.Loaded<*>>(loaded)
    }

    @Test
    fun `onCancelLoadSymbols handles both null and non-null job`() = runTest {
        val symbolsFake = FakeGetAvailableSymbolsUseCase(listOf(
            CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"),
        ))
        val viewModel = createViewModel(symbolsFake = symbolsFake)

        viewModel.filteredCoins.first { it !is Loadable.Loading }
        viewModel.onCancelLoadSymbols()
        viewModel.onCancelLoadSymbols()
    }

    @Test
    fun `onCancelLoadSymbols emits Cancelled when load is in progress`() = runTest {
        val loadStarted = MutableStateFlow(false)
        val getSymbols = object : GetAvailableSymbolsUseCase {
            override suspend fun invoke(): List<CryptoSymbol> {
                loadStarted.value = true
                awaitCancellation()
            }
        }
        val viewModel = CoinSearchViewModel(
            getAvailableSymbolsUseCase = getSymbols,
            toggleFavoriteUseCase = FakeToggleFavoriteUseCase(),
            observeFavoritesUseCase = FakeObserveFavoritesUseCase(),
            errorClassifier = fakeClassifier(),
        )

        loadStarted.first { it }
        viewModel.onCancelLoadSymbols()

        val cancelled = viewModel.filteredCoins.first { it is Loadable.Cancelled }
        assertIs<Loadable.Cancelled>(cancelled)
    }

    @Test
    fun `retryLoadSymbols reloads after failure`() = runTest {
        val symbolsFake = FakeGetAvailableSymbolsUseCase(exception = RuntimeException("fail"))
        val viewModel = createViewModel(symbolsFake = symbolsFake)

        viewModel.filteredCoins.first { it !is Loadable.Loading }

        symbolsFake.exception = null
        symbolsFake.symbols = listOf(CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"))

        viewModel.retryLoadSymbols()

        val result = viewModel.filteredCoins.first { it is Loadable.Loaded && it.value is Fallible.Success && (it.value as Fallible.Success<*>).value == listOf("BTCUSDT") }
        val symbols = extractSymbols(result)
        assertEquals(listOf("BTCUSDT"), symbols)
    }

    private fun createViewModel(
        symbolsFake: FakeGetAvailableSymbolsUseCase = FakeGetAvailableSymbolsUseCase(),
        toggleFake: FakeToggleFavoriteUseCase = FakeToggleFavoriteUseCase(),
        observeFake: FakeObserveFavoritesUseCase = FakeObserveFavoritesUseCase(),
    ): CoinSearchViewModel {
        return CoinSearchViewModel(symbolsFake, toggleFake, observeFake, fakeClassifier())
    }

    private fun fakeClassifier() = object : ErrorClassifier() {
        override fun isNetworkError(e: Throwable) = false
    }

    private fun extractSymbols(result: Loadable<*>): List<String> {
        val loaded = result as Loadable.Loaded
        val success = loaded.value as Fallible.Success<*>
        @Suppress("UNCHECKED_CAST")
        return (success.value as List<String>).sorted()
    }
}
