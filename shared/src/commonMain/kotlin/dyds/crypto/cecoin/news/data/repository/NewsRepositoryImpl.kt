package dyds.crypto.cecoin.news.data.repository

import dyds.crypto.cecoin.core.data.caching.CachedDataSource
import dyds.crypto.cecoin.news.domain.model.NewsArticle
import dyds.crypto.cecoin.news.domain.repository.NewsRepository

class NewsRepositoryImpl(
    private val cache: CachedDataSource<NewsArticle>,
) : NewsRepository {

    override suspend fun getCryptoNews(): List<NewsArticle> = cache.get()

    fun invalidateCache() = cache.invalidate()
}
