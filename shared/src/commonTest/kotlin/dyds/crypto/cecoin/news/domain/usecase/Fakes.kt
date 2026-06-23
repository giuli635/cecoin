package dyds.crypto.cecoin.news.domain.usecase

import dyds.crypto.cecoin.news.domain.model.NewsArticle
import dyds.crypto.cecoin.core.domain.state.Fallible
import dyds.crypto.cecoin.core.domain.error.AppError

class FakeGetCryptoNewsUseCase(
    var articles: List<NewsArticle> = emptyList(),
    var exception: Throwable? = null,
) : GetCryptoNewsUseCase {
    override suspend fun invoke(): Fallible<List<NewsArticle>> {
        if (exception != null) return Fallible.Failed(AppError.GenericError(exception!!, "fallo"))
        return Fallible.Success(articles)
    }
}
