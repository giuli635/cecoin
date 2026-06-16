package dyds.crypto.cecoin.data.repository

import dyds.crypto.cecoin.data.FakeNewsApiDataSource
import dyds.crypto.cecoin.domain.model.NewsArticle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
}
