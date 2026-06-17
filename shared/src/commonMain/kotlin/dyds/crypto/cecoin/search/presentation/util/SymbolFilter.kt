package dyds.crypto.cecoin.search.presentation.util

import dyds.crypto.cecoin.search.presentation.FilterMode

internal fun List<String>.filterBy(
    query: String,
    filterMode: FilterMode,
    favorites: Set<String>,
): List<String> {
    val filtered = when (filterMode) {
        FilterMode.ALL -> this
        FilterMode.FAVORITES -> filter { it in favorites }
    }
    return if (query.isEmpty()) filtered
    else filtered.filter { it.contains(query, ignoreCase = true) }
}
