package dyds.crypto.cecoin.search.domain.usecase

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.core.domain.state.Fallible
import dyds.crypto.cecoin.core.domain.error.AppError
import dyds.crypto.cecoin.core.domain.error.UiText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGetAvailableSymbolsUseCase(
    var symbols: List<CryptoSymbol> = emptyList(),
    var exception: Throwable? = null,
) : GetAvailableSymbolsUseCase {
    override suspend fun invoke(): Fallible<List<CryptoSymbol>> {
        if (exception != null) return Fallible.Failed(AppError.GenericError(exception!!, UiText.Dynamic("fallo")))
        return Fallible.Success(symbols)
    }
}

class FakeObserveFavoritesUseCase(
    initial: Set<CryptoSymbol> = emptySet(),
    private val flow: MutableStateFlow<Set<CryptoSymbol>> = MutableStateFlow(initial),
) : ObserveFavoritesUseCase {
    override fun invoke(): Flow<Set<CryptoSymbol>> = flow
}

class FakeToggleFavoriteUseCase(
    val favorites: MutableStateFlow<Set<CryptoSymbol>> = MutableStateFlow(emptySet()),
    var exception: Throwable? = null,
) : ToggleFavoriteUseCase {
    var lastToggled: CryptoSymbol? = null

    override suspend fun invoke(symbol: CryptoSymbol): Fallible<Unit> {
        if (exception != null) return Fallible.Failed(AppError.GenericError(exception!!, UiText.Dynamic("fallo")))
        lastToggled = symbol
        favorites.value = if (symbol in favorites.value) {
            favorites.value - symbol
        } else {
            favorites.value + symbol
        }
        return Fallible.Success(Unit)
    }
}
