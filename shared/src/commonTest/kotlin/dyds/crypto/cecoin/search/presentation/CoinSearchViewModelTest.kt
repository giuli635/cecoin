package dyds.crypto.cecoin.search.presentation

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.utils.fakeBtcSymbol
import dyds.crypto.cecoin.core.utils.fakeEthSymbol
import dyds.crypto.cecoin.core.utils.fakeBnbSymbol
import dyds.crypto.cecoin.search.domain.usecase.FakeGetAvailableSymbolsUseCase
import dyds.crypto.cecoin.search.domain.usecase.FakeObserveFavoritesUseCase
import dyds.crypto.cecoin.search.domain.usecase.FakeToggleFavoriteUseCase
import dyds.crypto.cecoin.search.domain.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.core.domain.error.AppError
import dyds.crypto.cecoin.core.domain.state.Fallible
import dyds.crypto.cecoin.core.domain.state.Loadable
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CoinSearchViewModelTest {

    @Test
    fun `init loads symbols and emits success`() = runTest {
        val symbolsFake = FakeGetAvailableSymbolsUseCase(listOf(
            fakeBtcSymbol,
            fakeEthSymbol,
        ))
        val viewModel = createViewModel(symbolsFake = symbolsFake)

        val result = viewModel.filteredCoins.first { it !is Loadable.Loading }

        val loaded = assertIs<Loadable.Loaded<Fallible<List<CryptoSymbol>>>>(result)
        val success = assertIs<Fallible.Success<List<CryptoSymbol>>>(loaded.value)
        assertEquals(
            listOf(fakeBtcSymbol, fakeEthSymbol),
            success.value.sorted(),
        )
    }

    @Test
    fun `loadSymbols emits failed when usecase fails`() = runTest {
        val symbolsFake = FakeGetAvailableSymbolsUseCase(exception = RuntimeException("API error"))
        val viewModel = createViewModel(symbolsFake = symbolsFake)

        val result = viewModel.filteredCoins.first { it !is Loadable.Loading }

        assertIs<Loadable.Loaded<*>>(result)
        val fallible = (result as Loadable.Loaded).value
        assertIs<Fallible.Failed>(fallible)
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
            fakeBtcSymbol,
            fakeEthSymbol,
            fakeBnbSymbol,
        ))
        val viewModel = createViewModel(symbolsFake = symbolsFake)

        viewModel.filteredCoins.first { it !is Loadable.Loading }

        viewModel.onSearchQueryChange("btc")

        val result = viewModel.filteredCoins.first { it is Loadable.Loaded && it.value is Fallible.Success && (it.value as Fallible.Success<*>).value == listOf(fakeBtcSymbol) }
        val symbols = extractSymbols(result)
        assertEquals(listOf(fakeBtcSymbol), symbols)
    }

    @Test
    fun `setFilterMode to FAVORITES shows only favorites`() = runTest {
        val symbolsFake = FakeGetAvailableSymbolsUseCase(listOf(
            fakeBtcSymbol,
            fakeEthSymbol,
        ))
        val observeFake = FakeObserveFavoritesUseCase(initial = setOf(fakeEthSymbol))
        val viewModel = createViewModel(symbolsFake = symbolsFake, observeFake = observeFake)

        viewModel.filteredCoins.first { it !is Loadable.Loading }
        viewModel.setFilterMode(FilterMode.FAVORITES)

        val result = viewModel.filteredCoins.first { it is Loadable.Loaded && it.value is Fallible.Success && (it.value as Fallible.Success<*>).value == listOf(fakeEthSymbol) }
        val symbols = extractSymbols(result)
        assertEquals(listOf(fakeEthSymbol), symbols)
    }

    @Test
    fun `setFilterMode to ALL shows all symbols`() = runTest {
        val symbolsFake = FakeGetAvailableSymbolsUseCase(listOf(
            fakeBtcSymbol,
            fakeEthSymbol,
        ))
        val viewModel = createViewModel(symbolsFake = symbolsFake)

        viewModel.filteredCoins.first { it !is Loadable.Loading }
        viewModel.setFilterMode(FilterMode.FAVORITES)
        viewModel.filteredCoins.first { it is Loadable.Loaded && it.value is Fallible.Success && (it.value as Fallible.Success<*>).value == emptyList<CryptoSymbol>() }
        viewModel.setFilterMode(FilterMode.ALL)

        val result = viewModel.filteredCoins.first { it is Loadable.Loaded && it.value is Fallible.Success && (it.value as Fallible.Success<*>).value == listOf(fakeBtcSymbol, fakeEthSymbol) }
        val symbols = extractSymbols(result)
        assertEquals(2, symbols.size)
    }

    @Test
    fun `setFilterMode to FAVORITES reacts to dynamic favorites changes`() = runTest {
        val symbolsFake = FakeGetAvailableSymbolsUseCase(listOf(fakeBtcSymbol, fakeEthSymbol))
        val sharedFavorites = MutableStateFlow(setOf(fakeBtcSymbol))
        val observeFake = FakeObserveFavoritesUseCase(flow = sharedFavorites)
        val toggleFake = FakeToggleFavoriteUseCase(favorites = sharedFavorites)
        val viewModel = createViewModel(symbolsFake = symbolsFake, toggleFake = toggleFake, observeFake = observeFake)

        viewModel.filteredCoins.first { it !is Loadable.Loading }
        viewModel.setFilterMode(FilterMode.FAVORITES)

        val btcOnly = viewModel.filteredCoins.first {
            it is Loadable.Loaded && it.value is Fallible.Success && (it.value as Fallible.Success<*>).value == listOf(fakeBtcSymbol)
        }
        assertEquals(listOf(fakeBtcSymbol), extractSymbols(btcOnly))

        sharedFavorites.value = setOf(fakeBtcSymbol, fakeEthSymbol)

        val both = viewModel.filteredCoins.first {
            it is Loadable.Loaded && it.value is Fallible.Success && (it.value as Fallible.Success<*>).value.let { s ->
                @Suppress("UNCHECKED_CAST")
                (s as List<CryptoSymbol>).containsAll(listOf(fakeBtcSymbol, fakeEthSymbol))
            }
        }
        assertEquals(2, extractSymbols(both).size)
    }

    @Test
    fun `toggleFavorite delegates to use case`() = runTest {
        val sharedFavorites = MutableStateFlow(emptySet<CryptoSymbol>())
        val toggleFake = FakeToggleFavoriteUseCase(favorites = sharedFavorites)
        val observeFake = FakeObserveFavoritesUseCase(flow = sharedFavorites)
        val viewModel = createViewModel(toggleFake = toggleFake, observeFake = observeFake)

        viewModel.toggleFavorite(fakeBtcSymbol)

        assertTrue(fakeBtcSymbol in viewModel.favorites.first { fakeBtcSymbol in it })
        assertEquals(fakeBtcSymbol, toggleFake.lastToggled)
    }

    @Test
    fun `toggleFavorite sets toggleError to null on success`() = runTest {
        val viewModel = createViewModel()

        viewModel.toggleFavorite(fakeBtcSymbol)

        assertNull(viewModel.toggleError.first { it == null })
    }

    @Test
    fun `toggleFavorite sets toggleError on failure`() = runTest {
        val toggleFake = FakeToggleFavoriteUseCase(exception = RuntimeException("fail"))
        val viewModel = createViewModel(toggleFake = toggleFake)

        viewModel.toggleFavorite(fakeBtcSymbol)

        val error = viewModel.toggleError.first { it != null }
        assertIs<AppError.GenericError>(error)
    }

    @Test
    fun `clearToggleError resets toggleError to null`() = runTest {
        val toggleFake = FakeToggleFavoriteUseCase(exception = RuntimeException("fail"))
        val viewModel = createViewModel(toggleFake = toggleFake)

        viewModel.toggleFavorite(fakeBtcSymbol)
        viewModel.toggleError.first { it != null }

        viewModel.clearToggleError()

        assertNull(viewModel.toggleError.first { it == null })
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
            override suspend fun invoke(): Fallible<List<CryptoSymbol>> =
                Fallible.Success(symbolsDeferred.await())
        }
        val viewModel = CoinSearchViewModel(
            getAvailableSymbolsUseCase = getSymbols,
            toggleFavoriteUseCase = FakeToggleFavoriteUseCase(),
            observeFavoritesUseCase = FakeObserveFavoritesUseCase(),
        )

        val initial = viewModel.filteredCoins.first()
        assertIs<Loadable.Loading>(initial)

        symbolsDeferred.complete(listOf(fakeBtcSymbol))

        val loaded = viewModel.filteredCoins.first { it !is Loadable.Loading }
        assertIs<Loadable.Loaded<*>>(loaded)
    }

    @Test
    fun `onCancelLoadSymbols emits Cancelled when load is in progress`() = runTest {
        val loadStarted = MutableStateFlow(false)
        val getSymbols = object : GetAvailableSymbolsUseCase {
            override suspend fun invoke(): Fallible<List<CryptoSymbol>> {
                loadStarted.value = true
                awaitCancellation()
            }
        }
        val viewModel = CoinSearchViewModel(
            getAvailableSymbolsUseCase = getSymbols,
            toggleFavoriteUseCase = FakeToggleFavoriteUseCase(),
            observeFavoritesUseCase = FakeObserveFavoritesUseCase(),
        )

        loadStarted.first { it }
        viewModel.onCancelLoadSymbols()

        val cancelled = viewModel.filteredCoins.first { it is Loadable.Cancelled }
        assertIs<Loadable.Cancelled>(cancelled)
    }

    @Test
    fun `cancel then retry loads symbols successfully`() = runTest {
        val symbolsDeferred = CompletableDeferred<List<CryptoSymbol>>()
        val getSymbols = object : GetAvailableSymbolsUseCase {
            override suspend fun invoke(): Fallible<List<CryptoSymbol>> =
                Fallible.Success(symbolsDeferred.await())
        }
        val viewModel = CoinSearchViewModel(
            getAvailableSymbolsUseCase = getSymbols,
            toggleFavoriteUseCase = FakeToggleFavoriteUseCase(),
            observeFavoritesUseCase = FakeObserveFavoritesUseCase(),
        )

        viewModel.filteredCoins.first { it is Loadable.Loading }
        viewModel.onCancelLoadSymbols()

        symbolsDeferred.complete(listOf(fakeBtcSymbol))
        viewModel.retryLoadSymbols()

        val result = viewModel.filteredCoins.first {
            it is Loadable.Loaded && it.value is Fallible.Success && (it.value as Fallible.Success<*>).value == listOf(fakeBtcSymbol)
        }
        assertEquals(listOf(fakeBtcSymbol), extractSymbols(result))
    }

    @Test
    fun `retryLoadSymbols reloads after failure`() = runTest {
        val symbolsFake = FakeGetAvailableSymbolsUseCase(exception = RuntimeException("fail"))
        val viewModel = createViewModel(symbolsFake = symbolsFake)

        viewModel.filteredCoins.first { it !is Loadable.Loading }

        symbolsFake.exception = null
        symbolsFake.symbols = listOf(fakeBtcSymbol)

        viewModel.retryLoadSymbols()

        val result = viewModel.filteredCoins.first { it is Loadable.Loaded && it.value is Fallible.Success && (it.value as Fallible.Success<*>).value == listOf(fakeBtcSymbol) }
        val symbols = extractSymbols(result)
        assertEquals(listOf(fakeBtcSymbol), symbols)
    }

    private fun createViewModel(
        symbolsFake: FakeGetAvailableSymbolsUseCase = FakeGetAvailableSymbolsUseCase(),
        toggleFake: FakeToggleFavoriteUseCase = FakeToggleFavoriteUseCase(),
        observeFake: FakeObserveFavoritesUseCase = FakeObserveFavoritesUseCase(),
    ): CoinSearchViewModel {
        return CoinSearchViewModel(symbolsFake, toggleFake, observeFake)
    }

    private fun extractSymbols(result: Loadable<*>): List<CryptoSymbol> {
        val loaded = assertIs<Loadable.Loaded<Fallible<List<CryptoSymbol>>>>(result)
        val success = assertIs<Fallible.Success<List<CryptoSymbol>>>(loaded.value)
        return success.value.sorted()
    }
}
