package dyds.crypto.cecoin.news.domain.repository

import dyds.crypto.cecoin.news.domain.model.NewsArticle

interface NewsRepository {
    suspend fun getCryptoNews(): List<NewsArticle>
}
