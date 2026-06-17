package dyds.crypto.cecoin.search.domain.usecase

import dyds.crypto.cecoin.search.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow

interface ObserveFavoritesUseCase {
    operator fun invoke(): Flow<Set<String>>
}

class ObserveFavoritesUseCaseImpl(
    private val repository: FavoriteRepository,
) : ObserveFavoritesUseCase {
    override operator fun invoke(): Flow<Set<String>> = repository.observeFavorites()
}
