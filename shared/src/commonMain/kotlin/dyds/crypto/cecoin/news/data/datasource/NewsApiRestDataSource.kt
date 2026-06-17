package dyds.crypto.cecoin.news.data.datasource

import dyds.crypto.cecoin.news.domain.model.NewsArticle
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val NEWSAPI_KEY = "746b7364141a4a959d1852444ac7111b"
private const val NEWSAPI_URL = "https://newsapi.org/v2/everything"
private const val NEWS_QUERY = "cryptocurrency OR bitcoin OR ethereum OR blockchain"
private const val NEWS_LANGUAGE = "es"
private const val NEWS_SORT_BY = "publishedAt"
private const val NEWS_PAGE_SIZE = 50

class NewsApiRestDataSource(
    private val http: HttpClient,
) : NewsApiDataSource {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchCryptoNews(): List<NewsArticle> {
        val url = buildString {
            append("$NEWSAPI_URL?q=${NEWS_QUERY.replace(" ", "%20")}")
            append("&language=$NEWS_LANGUAGE")
            append("&sortBy=$NEWS_SORT_BY")
            append("&pageSize=$NEWS_PAGE_SIZE")
            append("&apiKey=$NEWSAPI_KEY")
        }
        val raw: String = http.get(url).bodyAsText()
        return parseArticles(raw)
    }

    private fun parseArticles(jsonText: String): List<NewsArticle> {
        val root = json.parseToJsonElement(jsonText).jsonObject
        val status = root["status"]?.jsonPrimitive?.content
        if (status == "error") {
            val message = root["message"]?.jsonPrimitive?.content ?: "Unknown API error"
            throw Exception("NewsAPI error: $message")
        }
        val articlesArray = root["articles"]?.jsonArray ?: return emptyList()
        return articlesArray.mapNotNull { element ->
            val obj = element.jsonObject
            val source = obj["source"]?.jsonObject
            NewsArticle(
                title = (obj["title"] as? JsonPrimitive)?.content.orEmpty(),
                description = (obj["description"] as? JsonPrimitive)?.content.orEmpty(),
                url = (obj["url"] as? JsonPrimitive)?.content.orEmpty(),
                urlToImage = (obj["urlToImage"] as? JsonPrimitive)?.content,
                sourceName = (source?.get("name") as? JsonPrimitive)?.content.orEmpty(),
                publishedAt = (obj["publishedAt"] as? JsonPrimitive)?.content.orEmpty(),
            )
        }
    }
}
