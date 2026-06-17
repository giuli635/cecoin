package dyds.crypto.cecoin.domain.usecase

import dyds.crypto.cecoin.domain.model.NewsArticle
import dyds.crypto.cecoin.domain.repository.NewsRepository
import dyds.crypto.cecoin.utils.ErrorClassifier
import dyds.crypto.cecoin.utils.Fallible
import kotlinx.coroutines.CancellationException

interface GetCryptoNewsUseCase {
    suspend operator fun invoke(): Fallible<List<NewsArticle>>
}

class GetCryptoNewsUseCaseImpl(
    private val repository: NewsRepository,
    private val errorClassifier: ErrorClassifier,
) : GetCryptoNewsUseCase {
    override suspend operator fun invoke(): Fallible<List<NewsArticle>> {
        return try {
            Fallible.Success(repository.getCryptoNews())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Fallible.Failed(errorClassifier.classify(e, "Error al cargar noticias"))
        }
    }
}
