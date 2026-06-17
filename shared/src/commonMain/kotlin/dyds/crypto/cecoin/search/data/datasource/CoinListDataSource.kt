package dyds.crypto.cecoin.search.data.datasource

import dyds.crypto.cecoin.search.domain.model.CryptoSymbol

interface CoinListDataSource {
    suspend fun fetchSymbols(): List<CryptoSymbol>
}

