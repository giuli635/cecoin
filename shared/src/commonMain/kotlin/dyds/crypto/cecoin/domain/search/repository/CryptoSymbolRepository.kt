package dyds.crypto.cecoin.domain.search.repository

import dyds.crypto.cecoin.domain.search.model.CryptoSymbol

interface CryptoSymbolRepository {
    suspend fun getAvailableSymbols(): List<CryptoSymbol>
}
