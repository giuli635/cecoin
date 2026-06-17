package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.repository.FavoriteRepository

interface ToggleFavoriteUseCase {
    suspend operator fun invoke(symbol: String)
}

class ToggleFavoriteUseCaseImpl(
    private val repository: FavoriteRepository,
) : ToggleFavoriteUseCase {
    override suspend operator fun invoke(symbol: String) = repository.toggleFavorite(symbol)
}
