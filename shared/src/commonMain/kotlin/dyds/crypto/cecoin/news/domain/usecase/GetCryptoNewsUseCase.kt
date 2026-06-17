package dyds.crypto.cecoin.news.domain.usecase

import dyds.crypto.cecoin.news.domain.model.NewsArticle
import dyds.crypto.cecoin.news.domain.repository.NewsRepository
import dyds.crypto.cecoin.core.utils.ErrorStrings
import dyds.crypto.cecoin.core.utils.error.ErrorClassifier
import dyds.crypto.cecoin.core.utils.state.Fallible
import dyds.crypto.cecoin.core.utils.state.runCatchingCancellable
import dyds.crypto.cecoin.core.utils.state.toFallible

interface GetCryptoNewsUseCase {
    suspend operator fun invoke(): Fallible<List<NewsArticle>>
}

class GetCryptoNewsUseCaseImpl(
    private val repository: NewsRepository,
    private val errorClassifier: ErrorClassifier,
) : GetCryptoNewsUseCase {
    override suspend operator fun invoke(): Fallible<List<NewsArticle>> {
        return runCatchingCancellable { repository.getCryptoNews() }
            .toFallible(errorClassifier, ErrorStrings.LOAD_NEWS)
    }
}
