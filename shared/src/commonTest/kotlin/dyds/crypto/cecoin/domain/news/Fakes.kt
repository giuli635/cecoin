package dyds.crypto.cecoin.domain.news

import dyds.crypto.cecoin.domain.news.model.NewsArticle
import dyds.crypto.cecoin.domain.news.repository.NewsRepository

internal class FakeNewsRepository(
    var articles: List<NewsArticle> = emptyList(),
    var exception: Throwable? = null,
) : NewsRepository {

    override suspend fun getCryptoNews(): List<NewsArticle> {
        exception?.let { throw it }
        return articles
    }
}
