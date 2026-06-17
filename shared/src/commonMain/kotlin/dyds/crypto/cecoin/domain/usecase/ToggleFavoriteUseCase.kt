package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.repository.FavoriteRepository
import dyds.crypto.cecoin.utils.ErrorClassifier
import dyds.crypto.cecoin.utils.Fallible
import kotlinx.coroutines.CancellationException

interface ToggleFavoriteUseCase {
    suspend operator fun invoke(symbol: String): Fallible<Unit>
}

class ToggleFavoriteUseCaseImpl(
    private val repository: FavoriteRepository,
    private val errorClassifier: ErrorClassifier,
) : ToggleFavoriteUseCase {
    override suspend operator fun invoke(symbol: String): Fallible<Unit> {
        return try {
            repository.toggleFavorite(symbol)
            Fallible.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Fallible.Failed(errorClassifier.classify(e, "Error al cambiar favorito"))
        }
    }
}
