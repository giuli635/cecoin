package dyds.crypto.cecoin.data.remote

import dyds.crypto.cecoin.domain.model.NewsArticle

interface NewsApiDataSource {
    suspend fun fetchCryptoNews(): List<NewsArticle>
}
