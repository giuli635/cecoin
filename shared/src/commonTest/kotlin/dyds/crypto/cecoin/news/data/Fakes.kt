package dyds.crypto.cecoin.news.data

import dyds.crypto.cecoin.news.data.datasource.NewsApiDataSource
import dyds.crypto.cecoin.news.domain.model.NewsArticle

internal class FakeNewsApiDataSource(
    var articles: List<NewsArticle> = emptyList(),
    var exception: Throwable? = null,
) : NewsApiDataSource {
    var callCount = 0

    override suspend fun fetchCryptoNews(): List<NewsArticle> {
        callCount++
        exception?.let { throw it }
        return articles
    }
}
