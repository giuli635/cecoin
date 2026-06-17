package dyds.crypto.cecoin.domain.chart.model

data class TradePrice(
    val symbol: String,
    val pricePoint: PricePoint,
) {
    val price: Double get() = pricePoint.price
    val timestamp: Long get() = pricePoint.timestamp
}
