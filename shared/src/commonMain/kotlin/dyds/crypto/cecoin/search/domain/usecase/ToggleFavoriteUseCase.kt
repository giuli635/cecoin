package dyds.crypto.cecoin.search.domain.usecase

import cecoin.shared.generated.resources.*
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.search.domain.repository.FavoriteRepository
import dyds.crypto.cecoin.core.domain.error.ErrorClassifier
import dyds.crypto.cecoin.core.domain.state.Fallible
import dyds.crypto.cecoin.core.domain.state.runCatchingCancellable
import dyds.crypto.cecoin.core.domain.state.toFallible
import org.jetbrains.compose.resources.getString

interface ToggleFavoriteUseCase {
    suspend operator fun invoke(symbol: CryptoSymbol): Fallible<Unit>
}

class ToggleFavoriteUseCaseImpl(
    private val repository: FavoriteRepository,
    private val errorClassifier: ErrorClassifier,
) : ToggleFavoriteUseCase {
    override suspend operator fun invoke(symbol: CryptoSymbol): Fallible<Unit> {
        return runCatchingCancellable { repository.toggleFavorite(symbol) }
            .toFallible(errorClassifier, getString(Res.string.error_toggle_favorite))
    }
}
