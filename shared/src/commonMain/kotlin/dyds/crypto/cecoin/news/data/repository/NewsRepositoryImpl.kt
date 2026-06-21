package dyds.crypto.cecoin.news.data.repository

import dyds.crypto.cecoin.core.data.caching.CachedDataSource
import dyds.crypto.cecoin.news.data.datasource.NewsApiDataSource
import dyds.crypto.cecoin.news.domain.model.NewsArticle
import dyds.crypto.cecoin.news.domain.repository.NewsRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class NewsRepositoryImpl(
    private val dataSource: NewsApiDataSource,
    cacheTtl: Duration = DEFAULT_CACHE_TTL,
) : NewsRepository {

    private val cache = CachedDataSource(
        fetchBlock = dataSource::fetchCryptoNews,
        cacheTtl = cacheTtl,
    )

    override suspend fun getCryptoNews(): List<NewsArticle> = cache.get()

    fun invalidateCache() = cache.invalidate()

    companion object {
        val DEFAULT_CACHE_TTL: Duration = 2.minutes
    }
}
