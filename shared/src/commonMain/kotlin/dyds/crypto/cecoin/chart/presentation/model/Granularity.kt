package dyds.crypto.cecoin.chart.presentation.model

enum class Granularity(
    val label: String,
    val interval: String,
    val millis: Long,
) {
    M1("1m", "1m", 60_000L),
    M3("3m", "3m", 180_000L),
    M5("5m", "5m", 300_000L),
    M15("15m", "15m", 900_000L),
    M30("30m", "30m", 1_800_000L),
    H1("1h", "1h", 3_600_000L),
    H2("2h", "2h", 7_200_000L),
    H4("4h", "4h", 14_400_000L),
    H6("6h", "6h", 21_600_000L),
    H12("12h", "12h", 43_200_000L),
    D1("1d", "1d", 86_400_000L),
}
