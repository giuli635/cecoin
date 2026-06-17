package dyds.crypto.cecoin.search.presentation.util

import dyds.crypto.cecoin.core.domain.model.CryptoSymbol
import dyds.crypto.cecoin.search.presentation.FilterMode

internal fun List<CryptoSymbol>.filterBy(
    query: String,
    filterMode: FilterMode,
    favorites: Set<CryptoSymbol>,
): List<CryptoSymbol> {
    val filtered = when (filterMode) {
        FilterMode.ALL -> this
        FilterMode.FAVORITES -> filter { it in favorites }
    }
    return if (query.isEmpty()) filtered
    else filtered.filter { it.symbol.contains(query, ignoreCase = true) }
}
