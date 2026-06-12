package dyds.crypto.cecoin.domain.repository

import dyds.crypto.cecoin.domain.model.CryptoSymbol

interface CryptoSymbolRepository {
    suspend fun getAvailableSymbols(): List<CryptoSymbol>
}
