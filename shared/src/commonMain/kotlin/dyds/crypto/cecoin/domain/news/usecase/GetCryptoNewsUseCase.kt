package dyds.crypto.cecoin.domain.news.usecase

import dyds.crypto.cecoin.domain.news.model.NewsArticle
import dyds.crypto.cecoin.domain.news.repository.NewsRepository
import dyds.crypto.cecoin.utils.error.ErrorClassifier
import dyds.crypto.cecoin.utils.state.Fallible
import dyds.crypto.cecoin.utils.state.runCatchingCancellable
import dyds.crypto.cecoin.utils.state.toFallible

interface GetCryptoNewsUseCase {
    suspend operator fun invoke(): Fallible<List<NewsArticle>>
}

class GetCryptoNewsUseCaseImpl(
    private val repository: NewsRepository,
    private val errorClassifier: ErrorClassifier,
) : GetCryptoNewsUseCase {
    override suspend operator fun invoke(): Fallible<List<NewsArticle>> {
        return runCatchingCancellable { repository.getCryptoNews() }
            .toFallible(errorClassifier, "Error al cargar noticias")
    }
}
