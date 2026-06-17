package dyds.crypto.cecoin.search.domain.usecase

import dyds.crypto.cecoin.search.domain.model.CryptoSymbol
import dyds.crypto.cecoin.search.domain.repository.CryptoSymbolRepository
import dyds.crypto.cecoin.core.utils.ErrorStrings
import dyds.crypto.cecoin.core.utils.error.ErrorClassifier
import dyds.crypto.cecoin.core.utils.state.Fallible
import dyds.crypto.cecoin.core.utils.state.runCatchingCancellable
import dyds.crypto.cecoin.core.utils.state.toFallible

interface GetAvailableSymbolsUseCase {
    suspend operator fun invoke(): Fallible<List<CryptoSymbol>>
}

class GetAvailableSymbolsUseCaseImpl(
    private val repository: CryptoSymbolRepository,
    private val errorClassifier: ErrorClassifier,
) : GetAvailableSymbolsUseCase {
    override suspend operator fun invoke(): Fallible<List<CryptoSymbol>> {
        return runCatchingCancellable { repository.getAvailableSymbols() }
            .toFallible(errorClassifier, ErrorStrings.LOAD_SYMBOLS)
    }
}

