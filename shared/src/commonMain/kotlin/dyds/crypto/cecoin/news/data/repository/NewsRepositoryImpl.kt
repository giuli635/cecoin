package dyds.crypto.cecoin.news.data.repository

import dyds.crypto.cecoin.news.data.datasource.NewsApiDataSource
import dyds.crypto.cecoin.news.domain.model.NewsArticle
import dyds.crypto.cecoin.news.domain.repository.NewsRepository

class NewsRepositoryImpl(
    private val dataSource: NewsApiDataSource,
) : NewsRepository {
    override suspend fun getCryptoNews(): List<NewsArticle> {
        return dataSource.fetchCryptoNews()
    }
}
