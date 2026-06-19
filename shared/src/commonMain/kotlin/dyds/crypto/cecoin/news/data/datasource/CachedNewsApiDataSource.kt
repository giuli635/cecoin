package dyds.crypto.cecoin.news.data.datasource

import dyds.crypto.cecoin.news.domain.model.NewsArticle
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class CachedNewsApiDataSource(
    private val source: NewsApiDataSource,
    private val ttl: Duration = DEFAULT_TTL,
) : NewsApiDataSource {

    @Volatile
    private var cachedArticles: List<NewsArticle>? = null

    @Volatile
    private var lastFetchTimeMs: Long = NEVER_FETCHED

    private val mutex = Mutex()
    private val startMark = TimeSource.Monotonic.markNow()

    protected open fun now(): Long = startMark.elapsedNow().inWholeMilliseconds

    override suspend fun fetchCryptoNews(): List<NewsArticle> {
        cachedArticles?.let { articles ->
            if (lastFetchTimeMs != NEVER_FETCHED && now() - lastFetchTimeMs < ttl.inWholeMilliseconds)
                return articles
        }

        return mutex.withLock {
            cachedArticles?.let { articles ->
                if (lastFetchTimeMs != NEVER_FETCHED && now() - lastFetchTimeMs < ttl.inWholeMilliseconds)
                    return@withLock articles
            }

            val freshArticles = source.fetchCryptoNews()
            cachedArticles = freshArticles
            lastFetchTimeMs = now()
            freshArticles
        }
    }

    fun invalidateCache() {
        lastFetchTimeMs = NEVER_FETCHED
        cachedArticles = null
    }

    companion object {
        val DEFAULT_TTL: Duration = 2.minutes
        private const val NEVER_FETCHED = -1L
    }
}
