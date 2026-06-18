package dyds.crypto.cecoin.news.data.repository

import dyds.crypto.cecoin.news.data.FakeNewsApiDataSource
import dyds.crypto.cecoin.news.domain.model.NewsArticle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NewsRepositoryImplTest {
    @Test
    fun `getCryptoNews delegates to data source`() = runTest {
        val expected = listOf(
            NewsArticle("Title", "Desc", "url", null, "Source", "2024-01-01"),
        )
        val dataSource = FakeNewsApiDataSource(expected)
        val repo = NewsRepositoryImpl(dataSource)

        val result = repo.getCryptoNews()

        assertEquals(expected, result)
    }

    @Test
    fun `getCryptoNews returns empty list when data source returns empty`() = runTest {
        val dataSource = FakeNewsApiDataSource()
        val repo = NewsRepositoryImpl(dataSource)

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
        val repo = NewsRepositoryImpl(dataSource)

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
        val repo = NewsRepositoryImpl(dataSource)

        val result = repo.getCryptoNews()

        assertEquals(longStr, result.first().title)
        assertEquals(longStr, result.first().description)
    }

    @Test
    fun `getCryptoNews propagates source exception`() = runTest {
        val dataSource = FakeNewsApiDataSource(exception = RuntimeException("source fail"))
        val repo = NewsRepositoryImpl(dataSource)

        assertFailsWith<RuntimeException> {
            repo.getCryptoNews()
        }
    }
}
