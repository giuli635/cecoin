package dyds.crypto.cecoin.news.domain

import dyds.crypto.cecoin.news.domain.model.NewsArticle
import dyds.crypto.cecoin.news.domain.repository.NewsRepository

internal class FakeNewsRepository(
    var articles: List<NewsArticle> = emptyList(),
    var exception: Throwable? = null,
) : NewsRepository {

    override suspend fun getCryptoNews(): List<NewsArticle> {
        exception?.let { throw it }
        return articles
    }
}
