package dyds.crypto.cecoin.search.data.datasource

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol

interface CoinListDataSource {
    suspend fun fetchSymbols(): List<CryptoSymbol>
}

