package dyds.crypto.cecoin.data.news.repository

import dyds.crypto.cecoin.data.news.datasource.NewsApiDataSource
import dyds.crypto.cecoin.domain.news.model.NewsArticle
import dyds.crypto.cecoin.domain.news.repository.NewsRepository

class NewsRepositoryImpl(
    private val dataSource: NewsApiDataSource,
) : NewsRepository {
    override suspend fun getCryptoNews(): List<NewsArticle> {
        return dataSource.fetchCryptoNews()
    }
}
