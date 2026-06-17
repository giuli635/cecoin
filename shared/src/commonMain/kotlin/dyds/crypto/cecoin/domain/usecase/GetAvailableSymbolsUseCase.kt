package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.model.CryptoSymbol
import dyds.crypto.cecoin.domain.repository.CryptoSymbolRepository

interface GetAvailableSymbolsUseCase {
    suspend operator fun invoke(): List<CryptoSymbol>
}

class GetAvailableSymbolsUseCaseImpl(
    private val repository: CryptoSymbolRepository,
) : GetAvailableSymbolsUseCase {
    override suspend operator fun invoke(): List<CryptoSymbol> = repository.getAvailableSymbols()
}

