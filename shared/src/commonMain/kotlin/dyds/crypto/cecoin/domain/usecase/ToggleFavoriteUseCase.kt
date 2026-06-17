package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.repository.FavoriteRepository
import dyds.crypto.cecoin.utils.ErrorClassifier
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.runCatchingCancellable
import dyds.crypto.cecoin.utils.toFallible

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
