package dyds.crypto.cecoin.domain.news.usecase

import dyds.crypto.cecoin.domain.news.model.NewsArticle
import dyds.crypto.cecoin.domain.news.usecase.GetCryptoNewsUseCase
import dyds.crypto.cecoin.utils.state.Fallible
import dyds.crypto.cecoin.utils.error.AppError

class FakeGetCryptoNewsUseCase(
    var articles: List<NewsArticle> = emptyList(),
    var exception: Throwable? = null,
) : GetCryptoNewsUseCase {
    override suspend fun invoke(): Fallible<List<NewsArticle>> {
        if (exception != null) return Fallible.Failed(AppError.GenericError(exception!!, "fallo"))
        return Fallible.Success(articles)
    }
}
