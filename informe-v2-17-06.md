# Issues Pendientes — cecoin (17-Jun-2026)

**Versión:** v4 — 17 de Junio de 2026
**Propósito:** Issues activos para asignar al equipo.

---

## Prioridad 1 — SOLID y Clean Code

### [P1] God Object: `CecoinDependencyInjector`
- **Archivo:** `core/di/CecoinDependencyInjector.kt`
- **Problema:** Objeto conoce TODAS las dependencias del proyecto (4 data sources, 4 repos, 6 use cases, 3 VMs, 1 http client). Usa 8 `lateinit` frágiles — si `configure()` no se llama antes de cualquier getter, `UninitializedPropertyAccessException` en runtime.
- **Solución propuesta:** Dividir en módulos (`NetworkModule`, `DataModule`, `DomainModule`, `ViewModelModule`).
- **Archivos a crear:** 4-5 nuevos. **Archivos a modificar:** 1 (`CecoinDependencyInjector.kt` reducido o eliminado).

### [P1] `toggleFavorite()` traga errores silenciosamente
- **Archivo:** `search/presentation/CoinSearchViewModel.kt:879`
- **Problema:** `toggleFavoriteUseCase(symbol)` retorna `Fallible<Unit>` pero el resultado nunca se checkea. Si falla (DataStore error, escritura fallida), el error se traga sin feedback al usuario.
- **Solución propuesta:** Agregar `onFailure { _uiState.value = _uiState.value.copy(error = it) }`.
- **Archivo a modificar:** `CoinSearchViewModel.kt`

---

## Prioridad 2 — Tests

### [P2] Edge cases faltantes por use case

| Test | Happy Path | Error del repo | Empty data | Múltiples emisiones |
|---|---|---|---|---|
| `GetHistoricalPricesUseCaseTest` | ✅ | ❌ | ✅ | N/A |
| `ObserveTradePricesUseCaseTest` | ✅ | ✅ | ✅ | ✅ |
| `GetCryptoNewsUseCaseTest` | ✅ | ❌ | ❌ | N/A |
| `GetAvailableSymbolsUseCaseTest` | ✅ | ❌ | ❌ | N/A |
| `ObserveFavoritesUseCaseTest` | ✅ | N/A | ❌ | ❌ |
| `ToggleFavoriteUseCaseTest` | ✅ | ❌ | N/A | N/A |

**Qué falta:**
- `GetHistoricalPricesUseCaseTest` — test de fallo del repositorio (`Fallible.Failed`)
- `GetCryptoNewsUseCaseTest` — test de fallo + empty data
- `GetAvailableSymbolsUseCaseTest` — test de fallo + empty data
- `ToggleFavoriteUseCaseTest` — test de fallo del repositorio
- `ObserveFavoritesUseCaseTest` — flow vacío (`emptyFlow()`) + múltiples emisiones secuenciales

### [P2] Tests redundantes en `CoinSearchViewModelTest`
- **Archivo:** `search/presentation/CoinSearchViewModelTest.kt`
- **Problema:** 3 tests cubren esencialmente cancelación de `loadSymbolsJob`:
  - `onCancelLoadSymbols cancels active job` (Test A)
  - `onCancelLoadSymbols does nothing when no active job` (Test B)
  - `onCancelLoadSymbols handles both null and non-null job` (Test C)
- **Solución:** Consolidar en 2 tests (cancelación con job activo + null safety).

---

## Prioridad 3 — Código Muerto

### [P3] Eliminar estos elementos sin uso:

| # | Archivo | Elemento | Línea |
|---|---|---|---|
| 1 | `core/utils/TimeConstants.kt` | `MILLIS_IN_SECOND_DOUBLE = 1000.0` | 5 |
| 2 | `chart/presentation/Fakes.kt` (commonTest) | `fun fakeTradePricesFromPricePoints(vararg)` | 19 |
| 3 | `chart/presentation/Fakes.kt` (commonTest) | `class FakePriceAccumulator` | 43 |
| 4 | `chart/data/repository/ChartRepositoryImplTest.kt` | `val ethPricePoint` / `val ethTrade` | 70-72 |

---

## Prioridad 4 — Limpieza menor (opcional)

### [P4] Wrappers triviales y código cosmético

| # | Archivo | Problema | Sugerencia |
|---|---|---|---|
| 1 | `search/presentation/CoinSearchViewModel.kt` | `retryLoadSymbols()` solo llama a `loadSymbols()` | Eliminar o agregar lógica de reintento real |
| 2 | `search/presentation/CoinSearchViewModel.kt` | `onCancelLoadSymbols()` asigna `loadSymbolsJob = null` al final | Innecesario si `loadSymbolsJob` se reemplaza en la próxima `loadSymbols()` |

---
