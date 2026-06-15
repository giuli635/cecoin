package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.model.NewsArticle
import dyds.crypto.cecoin.domain.repository.NewsRepository

class GetCryptoNewsUseCase(
    private val repository: NewsRepository,
) {
    suspend operator fun invoke(): List<NewsArticle> {
        return repository.getCryptoNews()
    }
}
