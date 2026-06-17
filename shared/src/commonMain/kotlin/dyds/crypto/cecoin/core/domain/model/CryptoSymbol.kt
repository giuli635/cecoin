package dyds.crypto.cecoin.core.domain.model

@JvmInline
value class CryptoSymbol(val symbol: String) : Comparable<CryptoSymbol> {
    override fun compareTo(other: CryptoSymbol): Int = symbol.compareTo(other.symbol)
}
