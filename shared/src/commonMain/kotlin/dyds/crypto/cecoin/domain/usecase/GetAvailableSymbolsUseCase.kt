package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.model.CryptoSymbol
import dyds.crypto.cecoin.domain.repository.CryptoSymbolRepository

class GetAvailableSymbolsUseCase(
    private val repository: CryptoSymbolRepository,
) {
    suspend operator fun invoke(): List<CryptoSymbol> = repository.getAvailableSymbols()
}

