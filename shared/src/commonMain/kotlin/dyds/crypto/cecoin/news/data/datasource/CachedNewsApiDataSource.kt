package dyds.crypto.cecoin.news.data.datasource

import dyds.crypto.cecoin.news.domain.model.NewsArticle
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CachedNewsApiDataSource(
    private val source: NewsApiDataSource,
    private val ttl: Duration = DEFAULT_TTL,
) : NewsApiDataSource {

    @Volatile
    private var cachedArticles: List<NewsArticle>? = null

    @Volatile
    private var lastFetchTimeMark: TimeMark? = null

    private val mutex = Mutex()

    override suspend fun fetchCryptoNews(): List<NewsArticle> {
        cachedArticles?.let { articles ->
            lastFetchTimeMark?.let { mark ->
                if (mark.elapsedNow() < ttl) return articles
            }
        }

        return mutex.withLock {
            cachedArticles?.let { articles ->
                lastFetchTimeMark?.let { mark ->
                    if (mark.elapsedNow() < ttl) return@withLock articles
                }
            }

            val freshArticles = source.fetchCryptoNews()
            cachedArticles = freshArticles
            lastFetchTimeMark = TimeSource.Monotonic.markNow()
            freshArticles
        }
    }

    fun invalidateCache() {
        lastFetchTimeMark = null
        cachedArticles = null
    }

    companion object {
        val DEFAULT_TTL: Duration = 2.minutes
    }
}
