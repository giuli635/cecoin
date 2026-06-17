package dyds.crypto.cecoin.search.data.repository

import dyds.crypto.cecoin.search.data.datasource.CoinListDataSource
import dyds.crypto.cecoin.search.domain.model.CryptoSymbol
import dyds.crypto.cecoin.search.domain.repository.CryptoSymbolRepository

class SearchRepositoryImpl(
    private val coinListDataSource: CoinListDataSource,
) : CryptoSymbolRepository {

    override suspend fun getAvailableSymbols(): List<CryptoSymbol> =
        coinListDataSource.fetchSymbols()
}
