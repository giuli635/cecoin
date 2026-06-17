package dyds.crypto.cecoin.domain.search.usecase

import dyds.crypto.cecoin.domain.search.model.CryptoSymbol
import dyds.crypto.cecoin.domain.search.usecase.GetAvailableSymbolsUseCase
import dyds.crypto.cecoin.domain.search.usecase.ObserveFavoritesUseCase
import dyds.crypto.cecoin.domain.search.usecase.ToggleFavoriteUseCase
import dyds.crypto.cecoin.utils.state.Fallible
import dyds.crypto.cecoin.utils.error.AppError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGetAvailableSymbolsUseCase(
    var symbols: List<CryptoSymbol> = emptyList(),
    var exception: Throwable? = null,
) : GetAvailableSymbolsUseCase {
    override suspend fun invoke(): Fallible<List<CryptoSymbol>> {
        if (exception != null) return Fallible.Failed(AppError.GenericError(exception!!, "fallo"))
        return Fallible.Success(symbols)
    }
}

class FakeObserveFavoritesUseCase(
    initial: Set<String> = emptySet(),
    private val flow: MutableStateFlow<Set<String>> = MutableStateFlow(initial),
) : ObserveFavoritesUseCase {
    override fun invoke(): Flow<Set<String>> = flow
}

class FakeToggleFavoriteUseCase(
    val favorites: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet()),
) : ToggleFavoriteUseCase {
    var lastToggled: String? = null

    override suspend fun invoke(symbol: String): Fallible<Unit> {
        lastToggled = symbol
        favorites.value = if (symbol in favorites.value) {
            favorites.value - symbol
        } else {
            favorites.value + symbol
        }
        return Fallible.Success(Unit)
    }
}
