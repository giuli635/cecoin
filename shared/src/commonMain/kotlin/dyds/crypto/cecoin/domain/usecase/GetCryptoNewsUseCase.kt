package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.model.NewsArticle
import dyds.crypto.cecoin.domain.repository.NewsRepository

interface GetCryptoNewsUseCase {
    suspend operator fun invoke(): List<NewsArticle>
}

class GetCryptoNewsUseCaseImpl(
    private val repository: NewsRepository,
) : GetCryptoNewsUseCase {
    override suspend operator fun invoke(): List<NewsArticle> {
        return repository.getCryptoNews()
    }
}
