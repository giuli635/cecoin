package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.repository.FavoriteRepository

class ToggleFavoriteUseCase(
    private val repository: FavoriteRepository,
) {
    suspend operator fun invoke(symbol: String) = repository.toggleFavorite(symbol)
}
