package dyds.crypto.cecoin.data.search.datasource

import dyds.crypto.cecoin.domain.search.model.CryptoSymbol

interface CoinListDataSource {
    suspend fun fetchSymbols(): List<CryptoSymbol>
}

