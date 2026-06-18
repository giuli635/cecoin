package dyds.crypto.cecoin.news.data

import dyds.crypto.cecoin.news.data.datasource.NewsApiDataSource
import dyds.crypto.cecoin.news.domain.model.NewsArticle

internal class FakeNewsApiDataSource(
    private val articles: List<NewsArticle> = emptyList(),
    var exception: Throwable? = null,
) : NewsApiDataSource {
    override suspend fun fetchCryptoNews(): List<NewsArticle> {
        exception?.let { throw it }
        return articles
    }
}
