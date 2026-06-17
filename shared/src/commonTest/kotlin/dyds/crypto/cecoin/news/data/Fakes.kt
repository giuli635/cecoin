package dyds.crypto.cecoin.news.data

import dyds.crypto.cecoin.news.data.datasource.NewsApiDataSource
import dyds.crypto.cecoin.news.domain.model.NewsArticle

internal class FakeNewsApiDataSource(
    private val articles: List<NewsArticle> = emptyList(),
) : NewsApiDataSource {
    override suspend fun fetchCryptoNews(): List<NewsArticle> = articles
}
