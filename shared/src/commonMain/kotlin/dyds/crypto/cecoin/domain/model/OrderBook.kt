package dyds.crypto.cecoin.domain.model

data class OrderBook(
    val bids: List<OrderBookEntry>,
    val asks: List<OrderBookEntry>,
)

data class OrderBookEntry(
    val price: Double,
    val quantity: Double,
)
