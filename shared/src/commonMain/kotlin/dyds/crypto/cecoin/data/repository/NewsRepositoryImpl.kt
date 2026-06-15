package dyds.crypto.cecoin.data.repository

import dyds.crypto.cecoin.data.remote.NewsApiDataSource
import dyds.crypto.cecoin.domain.model.NewsArticle
import dyds.crypto.cecoin.domain.repository.NewsRepository

class NewsRepositoryImpl(
    private val dataSource: NewsApiDataSource,
) : NewsRepository {
    override suspend fun getCryptoNews(): List<NewsArticle> {
        return dataSource.fetchCryptoNews()
    }
}
