package dyds.crypto.cecoin.news.data.repository

import dyds.crypto.cecoin.core.data.caching.CachedDataSource
import dyds.crypto.cecoin.news.data.FakeNewsApiDataSource
import dyds.crypto.cecoin.news.domain.model.NewsArticle
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class NewsRepositoryImplTest {
    private val articleA = NewsArticle("Title A", "Desc", "url", null, "Source", "2024-01-01")
    private val articleB = NewsArticle("Title B", "Desc", "url", null, "Source", "2024-01-02")

    @Test
    fun `getCryptoNews delegates to data source on first call`() = runTest {
        val dataSource = FakeNewsApiDataSource(listOf(articleA))
        val repo = NewsRepositoryImpl(dataSource, CachedDataSource(dataSource::fetchCryptoNews, 2.minutes))

        val result = repo.getCryptoNews()

        assertEquals(listOf(articleA), result)
        assertEquals(1, dataSource.callCount)
    }

    @Test
    fun `getCryptoNews returns cached data without calling source on second call`() = runTest {
        val dataSource = FakeNewsApiDataSource(listOf(articleA, articleB))
        val repo = NewsRepositoryImpl(dataSource, CachedDataSource(dataSource::fetchCryptoNews, 2.minutes))

        repo.getCryptoNews()
        val result = repo.getCryptoNews()

        assertEquals(listOf(articleA, articleB), result)
        assertEquals(1, dataSource.callCount)
    }

    @Test
    fun `getCryptoNews calls source again when cache is invalidated`() = runTest {
        val dataSource = FakeNewsApiDataSource(listOf(articleA))
        val repo = NewsRepositoryImpl(dataSource, CachedDataSource(dataSource::fetchCryptoNews, 2.minutes))

        repo.getCryptoNews()
        assertEquals(1, dataSource.callCount)

        repo.invalidateCache()
        repo.getCryptoNews()

        assertEquals(2, dataSource.callCount)
    }

    @Test
    fun `getCryptoNews returns latest data after invalidation`() = runTest {
        val dataSource = FakeNewsApiDataSource(listOf(articleA))
        val repo = NewsRepositoryImpl(dataSource, CachedDataSource(dataSource::fetchCryptoNews, 2.minutes))

        repo.getCryptoNews()
        dataSource.articles = listOf(articleB)

        repo.invalidateCache()
        val result = repo.getCryptoNews()

        assertEquals(listOf(articleB), result)
    }

    @Test
    fun `concurrent calls only trigger one source fetch`() = runTest {
        val dataSource = FakeNewsApiDataSource(listOf(articleA))
        val repo = NewsRepositoryImpl(dataSource, CachedDataSource(dataSource::fetchCryptoNews, 2.minutes))

        coroutineScope {
            val jobs = (1..10).map {
                launch { repo.getCryptoNews() }
            }
            jobs.forEach { it.join() }
        }

        assertEquals(1, dataSource.callCount)
    }

    @Test
    fun `getCryptoNews returns empty list when data source returns empty`() = runTest {
        val dataSource = FakeNewsApiDataSource()
        val repo = NewsRepositoryImpl(dataSource, CachedDataSource(dataSource::fetchCryptoNews, 2.minutes))

        val result = repo.getCryptoNews()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getCryptoNews handles articles with empty fields`() = runTest {
        val articles = listOf(
            NewsArticle("", "", "", null, "", ""),
            NewsArticle(" ", " ", " ", null, " ", " "),
        )
        val dataSource = FakeNewsApiDataSource(articles)
        val repo = NewsRepositoryImpl(dataSource, CachedDataSource(dataSource::fetchCryptoNews, 2.minutes))

        val result = repo.getCryptoNews()

        assertEquals(articles, result)
    }

    @Test
    fun `getCryptoNews handles articles with very long strings`() = runTest {
        val longStr = "A".repeat(10_000)
        val articles = listOf(
            NewsArticle(longStr, longStr, longStr, null, longStr, longStr),
        )
        val dataSource = FakeNewsApiDataSource(articles)
        val repo = NewsRepositoryImpl(dataSource, CachedDataSource(dataSource::fetchCryptoNews, 2.minutes))

        val result = repo.getCryptoNews()

        assertEquals(longStr, result.first().title)
        assertEquals(longStr, result.first().description)
    }

    @Test
    fun `getCryptoNews propagates source exception`() = runTest {
        val dataSource = FakeNewsApiDataSource(exception = RuntimeException("source fail"))
        val repo = NewsRepositoryImpl(dataSource, CachedDataSource(dataSource::fetchCryptoNews, 2.minutes))

        assertFailsWith<RuntimeException> {
            repo.getCryptoNews()
        }
    }

    @Test
    fun `invalidateCache on empty cache does not crash`() = runTest {
        val dataSource = FakeNewsApiDataSource(listOf(articleA))
        val repo = NewsRepositoryImpl(dataSource, CachedDataSource(dataSource::fetchCryptoNews, 2.minutes))

        repo.invalidateCache()
        val result = repo.getCryptoNews()

        assertTrue(result.isNotEmpty())
    }
}
