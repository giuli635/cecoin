package dyds.crypto.cecoin.search.domain.repository

import dyds.crypto.cecoin.search.domain.model.CryptoSymbol

interface CryptoSymbolRepository {
    suspend fun getAvailableSymbols(): List<CryptoSymbol>
}
