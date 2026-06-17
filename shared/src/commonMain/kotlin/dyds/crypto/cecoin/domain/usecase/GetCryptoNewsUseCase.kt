package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.model.NewsArticle
import dyds.crypto.cecoin.domain.repository.NewsRepository
import dyds.crypto.cecoin.utils.ErrorClassifier
import dyds.crypto.cecoin.utils.Fallible
import dyds.crypto.cecoin.utils.runCatchingCancellable
import dyds.crypto.cecoin.utils.toFallible

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
