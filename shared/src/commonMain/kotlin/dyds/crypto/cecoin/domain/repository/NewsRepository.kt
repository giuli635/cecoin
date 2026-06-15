package dyds.crypto.cecoin.domain.repository

import dyds.crypto.cecoin.domain.model.NewsArticle

interface NewsRepository {
    suspend fun getCryptoNews(): List<NewsArticle>
}
