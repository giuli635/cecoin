package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow

class IsFavoriteUseCase(
    private val repository: FavoriteRepository,
) {
    operator fun invoke(symbol: String): Flow<Boolean> = repository.isFavorite(symbol)
}
