package dyds.crypto.cecoin.presentation

import dyds.crypto.cecoin.domain.model.CryptoSymbol
import dyds.crypto.cecoin.domain.repository.CryptoSymbolRepository
import dyds.crypto.cecoin.domain.repository.FavoriteRepository
import dyds.crypto.cecoin.domain.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.domain.usecase.ObserveFavoritesUseCase
import dyds.crypto.cecoin.domain.usecase.ToggleFavoriteUseCase
import dyds.crypto.cecoin.presentation.search.CoinSearchViewModel
import dyds.crypto.cecoin.presentation.search.FilterMode
import dyds.crypto.cecoin.utils.AppError
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.Loadable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class CoinSearchViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads symbols and emits success`() = runBlocking {
        val repo = FakeSymbolRepo(listOf(
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
    fun `loadSymbols emits failed when repository throws`() = runBlocking {
        val repo = FakeSymbolRepo(exception = RuntimeException("API error"))
        val viewModel = createViewModel(symbolRepo = repo)

        val result = viewModel.filteredCoins.first { it !is Loadable.Loading }

        assertIs<Loadable.Loaded<*>>(result)
        val fallible = (result as Loadable.Loaded).value
        assertIs<Fallible.Failed>(fallible)
        val error = (fallible as Fallible.Failed).error
        assertIs<AppError.GenericError>(error)
        assertTrue(error.userMessage.contains("Failed to load symbols"))
    }

    @Test
    fun `onSearchQueryChange updates search query in uiState`() = runBlocking {
        val viewModel = createViewModel()

        viewModel.onSearchQueryChange("BTC")

        val state = viewModel.uiState.first()
        assertEquals("BTC", state.searchQuery)
    }

    @Test
    fun `onSearchQueryChange filters coins`() = runBlocking {
        val repo = FakeSymbolRepo(listOf(
            CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"),
            CryptoSymbol("ETHUSDT", "ETH", "USDT", "TRADING"),
            CryptoSymbol("BNBUSDT", "BNB", "USDT", "TRADING"),
        ))
        val viewModel = createViewModel(symbolRepo = repo)

        viewModel.filteredCoins.first { it !is Loadable.Loading }

        viewModel.onSearchQueryChange("btc")

        val result = viewModel.filteredCoins.first()
        val symbols = extractSymbols(result)
        assertEquals(listOf("BTCUSDT"), symbols)
    }

    @Test
    fun `setFilterMode to FAVORITES shows only favorites`() = runBlocking {
        val repo = FakeSymbolRepo(listOf(
            CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"),
            CryptoSymbol("ETHUSDT", "ETH", "USDT", "TRADING"),
        ))
        val favRepo = createFakeFavRepo(initialFavorites = setOf("ETHUSDT"))
        val viewModel = createViewModel(symbolRepo = repo, favRepo = favRepo)

        viewModel.filteredCoins.first { it !is Loadable.Loading }

        viewModel.setFilterMode(FilterMode.FAVORITES)

        val result = viewModel.filteredCoins.first()
        val symbols = extractSymbols(result)
        assertEquals(listOf("ETHUSDT"), symbols)
    }

    @Test
    fun `setFilterMode to ALL shows all symbols`() = runBlocking {
        val repo = FakeSymbolRepo(listOf(
            CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"),
            CryptoSymbol("ETHUSDT", "ETH", "USDT", "TRADING"),
        ))
        val viewModel = createViewModel(symbolRepo = repo)

        viewModel.filteredCoins.first { it !is Loadable.Loading }
        viewModel.setFilterMode(FilterMode.FAVORITES)
        viewModel.filteredCoins.first()
        viewModel.setFilterMode(FilterMode.ALL)

        val result = viewModel.filteredCoins.first()
        val symbols = extractSymbols(result)
        assertEquals(2, symbols.size)
    }

    @Test
    fun `toggleFavorite delegates to use case`() = runBlocking {
        val favRepo = createFakeFavRepo()
        val viewModel = createViewModel(favRepo = favRepo)

        viewModel.toggleFavorite("BTCUSDT")

        val result = viewModel.favorites.first()
        assertTrue("BTCUSDT" in result)
    }

    @Test
    fun `retryLoadSymbols reloads after failure`() = runBlocking {
        val repo = FakeSymbolRepo(exception = RuntimeException("fail"))
        val viewModel = createViewModel(symbolRepo = repo)

        viewModel.filteredCoins.first { it !is Loadable.Loading }

        repo.exception = null
        repo.symbols = listOf(CryptoSymbol("BTCUSDT", "BTC", "USDT", "TRADING"))

        viewModel.retryLoadSymbols()

        val result = viewModel.filteredCoins.first { it !is Loadable.Loading }
        val symbols = extractSymbols(result)
        assertEquals(listOf("BTCUSDT"), symbols)
    }

    private fun createViewModel(
        symbolRepo: FakeSymbolRepo = FakeSymbolRepo(),
        favRepo: FakeFavRepo = createFakeFavRepo(),
    ): CoinSearchViewModel {
        val getSymbols = GetAvailableSymbolsUseCase(symbolRepo)
        val toggleFav = ToggleFavoriteUseCase(favRepo)
        val observeFav = ObserveFavoritesUseCase(favRepo)
        return CoinSearchViewModel(getSymbols, toggleFav, observeFav)
    }

    private fun createFakeFavRepo(initialFavorites: Set<String> = emptySet()): FakeFavRepo {
        val favFlow = MutableStateFlow(initialFavorites)
        return FakeFavRepo(favFlow)
    }

    private fun extractSymbols(result: Loadable<*>): List<String> {
        val loaded = result as Loadable.Loaded
        val success = loaded.value as Fallible.Success<*>
        @Suppress("UNCHECKED_CAST")
        return (success.value as List<String>).sorted()
    }
}

internal class FakeSymbolRepo(
    var symbols: List<CryptoSymbol> = emptyList(),
    var exception: Throwable? = null,
) : CryptoSymbolRepository {
    override suspend fun getAvailableSymbols(): List<CryptoSymbol> {
        exception?.let { throw it }
        return symbols
    }
}

internal class FakeFavRepo(
    private val favoritesFlow: MutableStateFlow<Set<String>>,
) : FavoriteRepository {
    override fun observeFavorites() = favoritesFlow
    override suspend fun toggleFavorite(symbol: String) {
        favoritesFlow.value = if (symbol in favoritesFlow.value) {
            favoritesFlow.value - symbol
        } else {
            favoritesFlow.value + symbol
        }
    }
}
