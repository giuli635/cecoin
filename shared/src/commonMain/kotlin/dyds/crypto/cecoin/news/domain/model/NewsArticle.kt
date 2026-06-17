package dyds.crypto.cecoin.news.domain.model

data class NewsArticle(
    val title: String,
    val description: String,
    val url: String,
    val urlToImage: String?,
    val sourceName: String,
    val publishedAt: String,
)
