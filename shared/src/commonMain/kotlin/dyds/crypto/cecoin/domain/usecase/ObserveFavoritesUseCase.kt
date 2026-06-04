package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow

class ObserveFavoritesUseCase(
    private val repository: FavoriteRepository,
) {
    operator fun invoke(): Flow<Set<String>> = repository.observeFavorites()
}
