package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.model.CryptoSymbol
import dyds.crypto.cecoin.domain.repository.CryptoSymbolRepository
import dyds.crypto.cecoin.utils.ErrorClassifier
import dyds.crypto.cecoin.utils.Fallible
import kotlinx.coroutines.CancellationException

interface GetAvailableSymbolsUseCase {
    suspend operator fun invoke(): Fallible<List<CryptoSymbol>>
}

class GetAvailableSymbolsUseCaseImpl(
    private val repository: CryptoSymbolRepository,
    private val errorClassifier: ErrorClassifier,
) : GetAvailableSymbolsUseCase {
    override suspend operator fun invoke(): Fallible<List<CryptoSymbol>> {
        return try {
            Fallible.Success(repository.getAvailableSymbols())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Fallible.Failed(errorClassifier.classify(e, "Error al cargar símbolos"))
        }
    }
}

