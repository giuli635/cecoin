package dyds.crypto.cecoin.domain.search.usecase

import dyds.crypto.cecoin.domain.search.model.CryptoSymbol
import dyds.crypto.cecoin.domain.search.repository.CryptoSymbolRepository
import dyds.crypto.cecoin.utils.error.ErrorClassifier
import dyds.crypto.cecoin.utils.state.Fallible
import dyds.crypto.cecoin.utils.state.runCatchingCancellable
import dyds.crypto.cecoin.utils.state.toFallible

interface GetAvailableSymbolsUseCase {
    suspend operator fun invoke(): Fallible<List<CryptoSymbol>>
}

class GetAvailableSymbolsUseCaseImpl(
    private val repository: CryptoSymbolRepository,
    private val errorClassifier: ErrorClassifier,
) : GetAvailableSymbolsUseCase {
    override suspend operator fun invoke(): Fallible<List<CryptoSymbol>> {
        return runCatchingCancellable { repository.getAvailableSymbols() }
            .toFallible(errorClassifier, "Error al cargar símbolos")
    }
}

