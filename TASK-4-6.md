# CeCoin — Data Layer Implementation Prompt

## Role

You are an expert Android/Kotlin developer working on a cryptocurrency app called **CeCoin**. The app follows Clean Architecture with MVVM. Your task is to implement the **data layer** for two features: **line chart price data** and **order book**, using Binance as the primary API and CoinCap as fallback/secondary.

---

## Architecture Rules

- Use the **Broker + Proxy pattern**, exactly as shown in the reference examples below.
- **Proxy**: maps remote data objects to domain objects. One proxy per API per feature.
- **Broker**: contains the merging/fallback logic. It knows both proxies.
- **Repository**: uses the broker. It knows nothing about specific APIs.
- Always prefer interfaces over concrete classes where possible.
- No comments in code. Clean, readable, self-documenting names.
- All files go under:
  - `src/kotlin/dyds/crypto/cecoin/data/remote`
  - `src/kotlin/dyds/crypto/cecoin/data/repository`

---

## Working Directories and Existing Code

The project already has a working WebSocket implementation that connects to Binance and emits price data as a `Flow<Double>`. It is located at:

```
src/kotlin/dyds/crypto/cecoin/data/remote/BinanceCoinPriceSource.kt
```

Its content is:

```kotlin
package dyds.crypto.cecoin.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val BASE_URLS = listOf(
    "wss://stream.binance.com:9443",
    "wss://stream.binance.com:443",
    "wss://data-stream.binance.vision",
)

class BinanceCoinPriceSource : CoinPriceSource {
    private val http = HttpClient {
        install(WebSockets)
    }
    private val json = Json { ignoreUnknownKeys = true }

    override fun tradePrices(symbol: String): Flow<Double> = flow {
        val stream = "${symbol.trim().lowercase()}@trade"
        var lastError: Throwable? = null
        for (baseUrl in BASE_URLS) {
            val url = "$baseUrl/ws/$stream"
            try {
                http.webSocket(urlString = url) {
                    for (frame in incoming) {
                        val text = (frame as? Frame.Text)?.readText() ?: continue
                        val price = parseTradePrice(text) ?: continue
                        emit(price)
                    }
                }
                return@flow
            } catch (throwable: Throwable) {
                lastError = throwable
            }
        }
        throw lastError ?: IllegalStateException("No se pudo abrir el WebSocket de Binance")
    }

    private fun parseTradePrice(message: String): Double? =
        runCatching {
            val root = json.parseToJsonElement(message).jsonObject
            root["p"]?.jsonPrimitive?.content?.toDouble()
        }.getOrNull()

    override fun close() {
        http.close()
    }
}
```

**Build on top of this existing file. Do not delete or rewrite it unless strictly necessary.**

---

## Features to Implement

### 1. Line Chart — Price Over Time (Fallback Pattern)

- **Primary**: Binance WebSocket `@trade` stream (already implemented in `BinanceCoinPriceSource`)
- **Fallback**: CoinCap WebSocket `wss://ws.coincap.io/prices?assets={asset}`
- **Logic**: try Binance first. Only if Binance fails (exception or disconnection), switch to CoinCap.
- Both emit `Flow<Double>` (price values over time).
- The broker collects the last N prices (e.g. 100) into a `Flow<List<Double>>` suitable for rendering a line chart.

### 2. Order Book (Merge Pattern)

- **Binance**: WebSocket `wss://stream.binance.com:9443/ws/{symbol}@depth20`
  - Payload fields: `bids` and `asks`, each a list of `[price, quantity]` string pairs.
- **CoinCap**: REST polling `GET https://api.coincap.io/v2/markets?baseId={asset}&limit=20`
  - Returns market data that can be used to approximate bids/asks.
- **Logic**:
  - Query both sources simultaneously.
  - If both respond: merge their bids and asks, sort bids descending by price, asks ascending by price, take top 20 of each.
  - If only one responds: use that one.
  - If neither responds: propagate the failure.

---

## Domain Objects

Define these in the domain layer if they don't exist:

```kotlin
data class OrderBook(
    val bids: List<OrderBookEntry>,
    val asks: List<OrderBookEntry>
)

data class OrderBookEntry(
    val price: Double,
    val quantity: Double
)
```

---

## Reference Examples from a Previous Project

Use these as the **exact structural reference** for naming conventions, layering, and patterns.

### Interface — External Source

```kotlin
interface PopularMoviesExternalSource {
    suspend fun getPopularMovies(): List<Movie>
}
```

### Interface — Details External Source

```kotlin
interface MovieDetailsExternalSource {
    suspend fun getMovieByTitle(title: String): Movie?
}
```

### Concrete External Source (Ktor HTTP Client)

```kotlin
internal class OMDBMoviesExternalSourceImpl : OMDBMoviesExternalSource {
    private val httpClient = HttpClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(DefaultRequest) {
            url {
                protocol = URLProtocol.HTTPS
                host = "www.omdbapi.com"
                parameters.append("apikey", OMDB_API_KEY)
            }
        }
        install(HttpTimeout) { requestTimeoutMillis = 5000 }
    }

    override suspend fun getMovie(title: String): OMDBRemoteMovie =
        httpClient.get("/") {
            url { parameters.append("t", title) }
        }.body()
}
```

### Remote Data Class

```kotlin
@Serializable
data class OMDBRemoteMovie(
    @SerialName(value = "Title") val title: String,
    @SerialName(value = "Plot") val plot: String,
    @SerialName(value = "imdbRating") val imdbRating: String,
)
```

### Proxy (Maps Remote → Domain, Wraps Failures)

```kotlin
internal class OMDBMoviesProxy(
    private val externalSource: OMDBMoviesExternalSource,
) : MovieDetailsExternalSource {
    override suspend fun getMovieByTitle(title: String): Movie? =
        runCatching { externalSource.getMovie(title).toDomainMovie() }
            .getOrNull()
}

private fun OMDBRemoteMovie.toDomainMovie(): Movie { ... }
```

### Broker (Merge Logic, Knows Both Proxies)

```kotlin
internal class MovieDetailsExternalSourceBroker(
    private val tmdbMoviesProxy: TMDBMoviesProxy,
    private val omdbMoviesProxy: OMDBMoviesProxy,
) : MovieDetailsExternalSource {
    override suspend fun getMovieByTitle(title: String): Movie? {
        val tmdbMovie = tmdbMoviesProxy.getMovieByTitle(title)
        val omdbMovie = omdbMoviesProxy.getMovieByTitle(title)
        return when {
            tmdbMovie != null && omdbMovie != null -> buildMovie(tmdbMovie, omdbMovie)
            tmdbMovie != null -> tmdbMovie.withSourceOverview("TMDB")
            omdbMovie != null -> omdbMovie.withSourceOverview("OMDB")
            else -> null
        }
    }

    private fun buildMovie(tmdbMovie: Movie, omdbMovie: Movie): Movie =
        Movie(
            popularity = (tmdbMovie.popularity + omdbMovie.popularity) / 2.0,
            voteAverage = (tmdbMovie.voteAverage + omdbMovie.voteAverage) / 2.0,
            ...
        )
}
```

### Repository (Uses Broker, Has Local Cache)

```kotlin
class MoviesRepositoryImpl(
    private val popularMoviesExternalSource: PopularMoviesExternalSource,
    private val movieDetailsExternalSource: MovieDetailsExternalSource,
    private val localMoviesSource: MoviesLocalSource
) : MoviesRepository {
    override suspend fun getPopularMovies(): List<Movie> {
        return localMoviesSource.getPopularMovies().ifEmpty {
            try {
                val movies = popularMoviesExternalSource.getPopularMovies()
                localMoviesSource.savePopularMovies(movies)
                movies
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
```

---

## File Naming Convention

Follow this exact structure:

```
data/remote/
  binance/
    BinanceCoinPriceSource.kt         ← already exists
    BinanceOrderBookSource.kt
    BinanceRemoteOrderBook.kt
  coincap/
    CoinCapPriceSource.kt
    CoinCapOrderBookSource.kt
    CoinCapRemoteMarket.kt
  CoinPriceSource.kt                  ← interface
  CoinOrderBookSource.kt              ← interface

data/remote/broker/
  CoinPriceSourceBroker.kt
  CoinOrderBookSourceBroker.kt

data/remote/proxy/
  BinancePriceSourceProxy.kt
  BinanceOrderBookSourceProxy.kt
  CoinCapPriceSourceProxy.kt
  CoinCapOrderBookSourceProxy.kt

data/repository/
  CryptoRepositoryImpl.kt
```

---

## Commit Strategy

Make one **atomic commit per unit of work**, in this order:

1. Domain objects `OrderBook` and `OrderBookEntry` if not present
2. `CoinPriceSource` and `CoinOrderBookSource` interfaces
3. Binance order book: remote data class + external source + proxy
4. CoinCap price source: remote + external source + proxy
5. CoinCap order book: remote data class + external source + proxy
6. `CoinPriceSourceBroker` (fallback logic)
7. `CoinOrderBookSourceBroker` (merge logic)
8. `CryptoRepositoryImpl`

Do not bundle multiple features in a single commit. Each commit message should describe exactly what was added.
