package dyds.crypto.cecoin.news.domain.usecase

import cecoin.shared.generated.resources.*
import dyds.crypto.cecoin.news.domain.model.NewsArticle
import dyds.crypto.cecoin.news.domain.repository.NewsRepository
import dyds.crypto.cecoin.core.domain.error.ErrorClassifier
import dyds.crypto.cecoin.core.domain.state.Fallible
import dyds.crypto.cecoin.core.domain.state.runCatchingCancellable
import dyds.crypto.cecoin.core.domain.state.toFallible
import org.jetbrains.compose.resources.getString

interface GetCryptoNewsUseCase {
    suspend operator fun invoke(): Fallible<List<NewsArticle>>
}

class GetCryptoNewsUseCaseImpl(
    private val repository: NewsRepository,
    private val errorClassifier: ErrorClassifier,
    private val lazyMessage: suspend () -> String = { getString(Res.string.error_load_news) },
) : GetCryptoNewsUseCase {
    override suspend operator fun invoke(): Fallible<List<NewsArticle>> {
        return runCatchingCancellable { repository.getCryptoNews() }
            .toFallible(errorClassifier, lazyMessage)
    }
}
