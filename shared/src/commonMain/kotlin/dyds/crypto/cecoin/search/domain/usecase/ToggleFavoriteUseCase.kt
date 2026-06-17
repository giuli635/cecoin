package dyds.crypto.cecoin.search.domain.usecase

import dyds.crypto.cecoin.search.domain.repository.FavoriteRepository
import dyds.crypto.cecoin.core.utils.error.ErrorClassifier
import dyds.crypto.cecoin.core.utils.state.Fallible
import dyds.crypto.cecoin.core.utils.state.runCatchingCancellable
import dyds.crypto.cecoin.core.utils.state.toFallible

interface ToggleFavoriteUseCase {
    suspend operator fun invoke(symbol: String): Fallible<Unit>
}

class ToggleFavoriteUseCaseImpl(
    private val repository: FavoriteRepository,
    private val errorClassifier: ErrorClassifier,
) : ToggleFavoriteUseCase {
    override suspend operator fun invoke(symbol: String): Fallible<Unit> {
        return runCatchingCancellable { repository.toggleFavorite(symbol) }
            .toFallible(errorClassifier, "Error al cambiar favorito")
    }
}
