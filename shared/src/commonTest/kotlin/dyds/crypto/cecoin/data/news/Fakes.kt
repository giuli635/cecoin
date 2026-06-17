package dyds.crypto.cecoin.data.news

import dyds.crypto.cecoin.data.news.datasource.NewsApiDataSource
import dyds.crypto.cecoin.domain.news.model.NewsArticle

internal class FakeNewsApiDataSource(
    private val articles: List<NewsArticle> = emptyList(),
) : NewsApiDataSource {
    override suspend fun fetchCryptoNews(): List<NewsArticle> = articles
}
