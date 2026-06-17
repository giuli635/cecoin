package dyds.crypto.cecoin.domain.news.repository

import dyds.crypto.cecoin.domain.news.model.NewsArticle

interface NewsRepository {
    suspend fun getCryptoNews(): List<NewsArticle>
}
