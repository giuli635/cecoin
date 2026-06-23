package dyds.crypto.cecoin.search.domain.usecase

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.search.domain.repository.CryptoSymbolRepository
import dyds.crypto.cecoin.core.domain.error.ErrorClassifier
import dyds.crypto.cecoin.core.domain.state.Fallible
import dyds.crypto.cecoin.core.domain.state.runCatchingCancellable
import dyds.crypto.cecoin.core.domain.state.toFallible

interface GetAvailableSymbolsUseCase {
    suspend operator fun invoke(): Fallible<List<CryptoSymbol>>
}

class GetAvailableSymbolsUseCaseImpl(
    private val repository: CryptoSymbolRepository,
    private val errorClassifier: ErrorClassifier,
    private val lazyMessage: suspend () -> String,
) : GetAvailableSymbolsUseCase {
    override suspend operator fun invoke(): Fallible<List<CryptoSymbol>> {
        return runCatchingCancellable { repository.getAvailableSymbols() }
            .toFallible(errorClassifier, lazyMessage)
    }
}
