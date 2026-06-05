package dyds.crypto.cecoin.domain.model

data class PricePoint(
    val timestamp: Long,
    val price: Double,
)

fun TradePrice.toPricePoint(bucketMillis: Long): PricePoint {
    val t = (timestamp / bucketMillis) * bucketMillis
    return PricePoint(t, price)
}

fun List<TradePrice>.toPricePoints(bucketMillis: Long): List<PricePoint> =
    map { it.toPricePoint(bucketMillis) }
