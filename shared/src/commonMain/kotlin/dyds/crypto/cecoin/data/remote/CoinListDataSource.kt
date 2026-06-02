package dyds.crypto.cecoin.data.remote

import dyds.crypto.cecoin.domain.model.CryptoSymbol

interface CoinListDataSource {
    suspend fun fetchSymbols(): List<CryptoSymbol>
}

