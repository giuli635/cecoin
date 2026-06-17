package dyds.crypto.cecoin.news.data.datasource

import dyds.crypto.cecoin.news.domain.model.NewsArticle

interface NewsApiDataSource {
    suspend fun fetchCryptoNews(): List<NewsArticle>
}
