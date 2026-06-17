package dyds.crypto.cecoin.data.search.repository

import dyds.crypto.cecoin.data.search.datasource.CoinListDataSource
import dyds.crypto.cecoin.domain.search.model.CryptoSymbol
import dyds.crypto.cecoin.domain.search.repository.CryptoSymbolRepository

class SearchRepositoryImpl(
    private val coinListDataSource: CoinListDataSource,
) : CryptoSymbolRepository {

    override suspend fun getAvailableSymbols(): List<CryptoSymbol> =
        coinListDataSource.fetchSymbols()
}
