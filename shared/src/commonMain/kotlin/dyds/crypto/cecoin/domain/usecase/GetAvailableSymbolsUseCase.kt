package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.model.CryptoSymbol
import dyds.crypto.cecoin.domain.repository.CecoinRepository

class GetAvailableSymbolsUseCase(
    private val repository: CecoinRepository,
) {
    suspend operator fun invoke(): List<CryptoSymbol> = repository.getAvailableSymbols()
}

