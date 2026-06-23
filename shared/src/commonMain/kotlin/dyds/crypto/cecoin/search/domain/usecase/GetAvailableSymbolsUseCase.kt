package dyds.crypto.cecoin.search.domain.usecase

import cecoin.shared.generated.resources.*
import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.search.domain.repository.CryptoSymbolRepository
import dyds.crypto.cecoin.core.domain.error.ErrorClassifier
import dyds.crypto.cecoin.core.domain.state.Fallible
import dyds.crypto.cecoin.core.domain.state.runCatchingCancellable
import dyds.crypto.cecoin.core.domain.state.toFallible
import org.jetbrains.compose.resources.getString

interface GetAvailableSymbolsUseCase {
    suspend operator fun invoke(): Fallible<List<CryptoSymbol>>
}

class GetAvailableSymbolsUseCaseImpl(
    private val repository: CryptoSymbolRepository,
    private val errorClassifier: ErrorClassifier,
) : GetAvailableSymbolsUseCase {
    override suspend operator fun invoke(): Fallible<List<CryptoSymbol>> {
        return runCatchingCancellable { repository.getAvailableSymbols() }
            .toFallible(errorClassifier, getString(Res.string.error_load_symbols))
    }
}
