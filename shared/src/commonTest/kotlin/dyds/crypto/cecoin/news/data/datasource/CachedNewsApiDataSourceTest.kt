package dyds.crypto.cecoin.news.data.datasource

import dyds.crypto.cecoin.core.utils.FakeTimeProvider
import dyds.crypto.cecoin.news.domain.model.NewsArticle
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CachedNewsApiDataSourceTest {

    private val articleA = NewsArticle("Title A", "Desc", "url", null, "Source", "2024-01-01")
    private val articleB = NewsArticle("Title B", "Desc", "url", null, "Source", "2024-01-02")

    @Test
    fun `fetchCryptoNews delegates to source on first call`() = runTest {
        val source = FakeNewsApiDataSource(listOf(articleA))
        val cache = CachedNewsApiDataSource(source, ttl = 2.minutes)

        val result = cache.fetchCryptoNews()

        assertEquals(listOf(articleA), result)
        assertEquals(1, source.callCount)
    }

    @Test
    fun `fetchCryptoNews returns cached data without calling source on second call`() = runTest {
        val source = FakeNewsApiDataSource(listOf(articleA, articleB))
        val cache = CachedNewsApiDataSource(source, ttl = 2.minutes)

        cache.fetchCryptoNews()
        val result = cache.fetchCryptoNews()

        assertEquals(listOf(articleA, articleB), result)
        assertEquals(1, source.callCount)
    }

    @Test
    fun `fetchCryptoNews calls source again when cache is invalidated`() = runTest {
        val source = FakeNewsApiDataSource(listOf(articleA))
        val cache = CachedNewsApiDataSource(source, ttl = 2.minutes)

        cache.fetchCryptoNews()
        assertEquals(1, source.callCount)

        cache.invalidateCache()
        cache.fetchCryptoNews()

        assertEquals(2, source.callCount)
    }

    @Test
    fun `fetchCryptoNews returns latest data after invalidation`() = runTest {
        val initial = listOf(articleA)
        val updated = listOf(articleB)
        val source = FakeNewsApiDataSource(initial)
        val cache = CachedNewsApiDataSource(source, ttl = 2.minutes)

        cache.fetchCryptoNews()
        source.articles = updated

        cache.invalidateCache()
        val result = cache.fetchCryptoNews()

        assertEquals(listOf(articleB), result)
    }

    @Test
    fun `fetchCryptoNews calls source again when TTL expires`() = runTest {
        val timeProvider = FakeTimeProvider()
        val source = FakeNewsApiDataSource(listOf(articleA, articleB))
        val cache = object : CachedNewsApiDataSource(source, ttl = 1.minutes) {
            override fun now(): Long = timeProvider()
        }

        cache.fetchCryptoNews()
        assertEquals(1, source.callCount)

        timeProvider.advanceBy(61000)

        val result = cache.fetchCryptoNews()

        assertEquals(listOf(articleA, articleB), result)
        assertEquals(2, source.callCount)
    }

    @Test
    fun `concurrent calls after TTL expiry only trigger one source fetch`() = runTest {
        val timeProvider = FakeTimeProvider()
        val source = FakeNewsApiDataSource(
            articles = listOf(articleA, articleB),
            fetchDelay = 10.milliseconds,
        )
        val cache = object : CachedNewsApiDataSource(source, ttl = 1.minutes) {
            override fun now(): Long = timeProvider()
        }

        cache.fetchCryptoNews()
        timeProvider.advanceBy(61000)

        coroutineScope {
            val jobs = (1..10).map {
                launch { cache.fetchCryptoNews() }
            }
            jobs.forEach { it.join() }
        }

        assertEquals(2, source.callCount)
    }

    @Test
    fun `concurrent calls only trigger one source fetch`() = runTest {
        val source = FakeNewsApiDataSource(listOf(articleA))
        val cache = CachedNewsApiDataSource(source, ttl = 2.minutes)

        coroutineScope {
            val jobs = (1..10).map {
                launch { cache.fetchCryptoNews() }
            }
            jobs.forEach { it.join() }
        }

        assertEquals(1, source.callCount)
    }

    private class FakeNewsApiDataSource(
        var articles: List<NewsArticle>,
        private val fetchDelay: Duration = Duration.ZERO,
    ) : NewsApiDataSource {
        var callCount = 0

        override suspend fun fetchCryptoNews(): List<NewsArticle> {
            callCount++
            if (fetchDelay > Duration.ZERO) delay(fetchDelay)
            return articles
        }
    }
}
