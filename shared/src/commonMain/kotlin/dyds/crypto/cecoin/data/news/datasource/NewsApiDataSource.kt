package dyds.crypto.cecoin.data.news.datasource

import dyds.crypto.cecoin.domain.news.model.NewsArticle

interface NewsApiDataSource {
    suspend fun fetchCryptoNews(): List<NewsArticle>
}
