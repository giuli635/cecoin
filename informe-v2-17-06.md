# Issues Pendientes — cecoin (17-Jun-2026)

Versión v4 — Issues activos para asignar al equipo. Cada entrada es autocontenida: describe el problema, dónde está, y qué hacer.

---

## Prioridad 1 — SOLID y Clean Code

### [P1] God Object: `CecoinDependencyInjector`

**Archivo:** `core/di/CecoinDependencyInjector.kt`

**Problema:**
Este objeto conoce TODAS las dependencias del proyecto: 4 data sources, 4 repositorios, 6 use cases, 3 ViewModels, 1 HttpClient. Usa 8 propiedades `lateinit` — si `configure()` no se llama antes de cualquier getter, explota con `UninitializedPropertyAccessException` en runtime.

```kotlin
object CecoinDependencyInjector {
    lateinit var errorClassifier: ErrorClassifier        // frágil
    private lateinit var priceAccumulatorFactory: ...    // frágil
    private lateinit var observePricesUseCase: ...       // frágil
    // ... 5 más
}
```

**Qué hacer:**
Dividir en módulos por responsabilidad. Ejemplo:

```kotlin
// NetworkModule.kt
object NetworkModule {
    val httpClient: HttpClient by lazy { HttpClient { install(WebSockets) } }
}

// DataModule.kt
object DataModule {
    val chartRepository: TradePriceRepository
        get() = ChartRepositoryImpl(coinPriceSource, coinHistoricalSource)
    // ...
}

// DomainModule.kt — construye use cases
// ViewModelModule.kt — factories @Composable
```

**Archivos:** crear 4-5 nuevos. Reducir o eliminar `CecoinDependencyInjector.kt`.

---

### [P1] `toggleFavorite()` traga errores silenciosamente

**Archivo:** `search/presentation/CoinSearchViewModel.kt`

**Problema:**
El método llama al use case pero ignora el resultado:

```kotlin
fun toggleFavorite(symbol: CryptoSymbol) {
    viewModelScope.launch {
        toggleFavoriteUseCase(symbol)   // Fallible<Unit> ignorado
    }
}
```

Si falla (error de DataStore, escritura fallida), el usuario nunca lo sabe. La UI no se actualiza, el favorito no se persiste, y nadie se entera.

**Qué hacer:**
- Agregar `onFailure` al resultado del use case.
- Propagar el error al `CoinSearchUiState` para mostrarlo en pantalla:

```kotlin
fun toggleFavorite(symbol: CryptoSymbol) {
    viewModelScope.launch {
        toggleFavoriteUseCase(symbol)
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error)
            }
    }
}
```

**Archivo a modificar:** `CoinSearchViewModel.kt`
**Opcional:** Agregar campo `error: AppError?` en `CoinSearchUiState.kt` si no existe.

---

## Prioridad 2 — Tests

### [P2] Edge cases faltantes por use case

Cada use case test necesita cubrir **3 escenarios**: happy path, error del repositorio, y empty data (cuando aplica).

| Test | Happy | Repo error | Empty data | Flujo múltiple |
|---|---|---|---|---|
| `GetHistoricalPricesUseCaseTest` | ✅ | ❌ | ✅ | N/A |
| `ObserveTradePricesUseCaseTest` | ✅ | ✅ | ✅ | ✅ |
| `GetCryptoNewsUseCaseTest` | ✅ | ❌ | ❌ | N/A |
| `GetAvailableSymbolsUseCaseTest` | ✅ | ❌ | ❌ | N/A |
| `ObserveFavoritesUseCaseTest` | ✅ | N/A | ❌ | ❌ |
| `ToggleFavoriteUseCaseTest` | ✅ | ❌ | N/A | N/A |

**Archivos de test (uno por use case):**

| Use Case Test | Ubicación |
|---|---|
| `GetHistoricalPricesUseCaseTest` | `chart/domain/usecase/GetHistoricalPricesUseCaseTest.kt` |
| `GetCryptoNewsUseCaseTest` | `news/domain/usecase/GetCryptoNewsUseCaseTest.kt` |
| `GetAvailableSymbolsUseCaseTest` | `search/domain/usecase/GetAvailableSymbolsUseCaseTest.kt` |
| `ToggleFavoriteUseCaseTest` | `search/domain/usecase/ToggleFavoriteUseCaseTest.kt` |
| `ObserveFavoritesUseCaseTest` | `search/domain/usecase/ObserveFavoritesUseCaseTest.kt` |

**Qué testear en cada uno:**

**a) Test de error del repositorio** (falta en 4 tests):
Configurar el FakeRepository para que lance una excepción. Llamar al use case. Verificar que devuelve `Fallible.Failed` con `AppError.GenericError`.

```kotlin
// Ejemplo para GetHistoricalPricesUseCaseTest
@Test
fun `invoke when repository throws returns Fallible Failed`() = runTest {
    val repo = FakePriceRepository()
    repo.setThrowOnGetHistorical(true)  // o similar según el Fake
    val useCase = GetHistoricalPricesUseCaseImpl(repo, fakeErrorClassifier())
    val result = useCase(fakeBtcSymbol)
    assertIs<Fallible.Failed>(result)
    assertIs<AppError.GenericError>((result as Fallible.Failed).error)
}
```

**b) Test de empty data** (falta en 3 tests): FakeRepository devuelve lista vacía. Verificar `Fallible.Success(emptyList())`.

**c) `ObserveFavoritesUseCaseTest`**: Agregar test con `emptyFlow()` (verifica que no crashea) y test con múltiples emisiones secuenciales (toggle 3 símbolos, verificar que el flow refleja cada cambio).

---

### [P2] Tests redundantes en `CoinSearchViewModelTest`

**Archivo:** `search/presentation/CoinSearchViewModelTest.kt`

**Problema:**
3 tests cubren el mismo comportamiento de cancelación:

1. `onCancelLoadSymbols cancels active job`
2. `onCancelLoadSymbols does nothing when no active job`
3. `onCancelLoadSymbols handles both null and non-null job`

El test 1 y el test 3 verifican lo mismo (cancelar un job activo). El test 2 y la segunda mitad del test 3 verifican lo mismo (null job no crashea).

**Qué hacer:**
- Fusionar en **2 tests**:
  - `onCancelLoadSymbols cancels active job and emits Cancelled` (job activo → se cancela + estado pasa a `Loadable.Cancelled`)
  - `onCancelLoadSymbols does nothing when no active job` (null safety, sin crash)

Adicionalmente mantener el test existente `onCancelLoadSymbols emits Cancelled when load is in progress` que verifica el estado, es diferente y válido.

---

## Prioridad 3 — Código Muerto

### [P3] Eliminar estos elementos sin uso

| # | Archivo | Línea | Elemento | Por qué eliminarlo |
|---|---|---|---|---|
| 1 | `core/utils/TimeConstants.kt` | 5 | `const val MILLIS_IN_SECOND_DOUBLE = 1000.0` | Definida pero nunca importada en ningún archivo del proyecto |
| 2 | `chart/presentation/Fakes.kt` (commonTest) | ~19 | `fun fakeTradePricesFromPricePoints(vararg pricePoints: PricePoint): List<TradePrice>` | Función helper nunca invocada por ningún test. La versión singular `fakeTradePriceFromPricePoint` sí se usa |
| 3 | `chart/presentation/Fakes.kt` (commonTest) | ~43 | `class FakePriceAccumulator(historical: List<TradePrice> = emptyList()) : PriceAccumulator` | Implementación fake de `PriceAccumulator` que nunca se instancia. Todos los tests usan `PriceAccumulatorImpl` real |
| 4 | `chart/data/repository/ChartRepositoryImplTest.kt` | 70-72 | `val ethPricePoint = PricePoint(2000L, 3000.0)` y `val ethTrade = TradePrice("ETHUSDT", ethPricePoint)` | `ethTrade` nunca se referencia en ningún test. `ethPricePoint` solo existe para construir `ethTrade`. Son residuos de tests planeados |

**Instrucción:** Simplemente borrar las líneas. Para #2 y #3, borrar toda la función/clase. Para #4, borrar las dos variables. Si en el futuro se necesitan, se pueden recrear rápidamente.

---

## Prioridad 4 — Limpieza menor (opcional)

### [P4] Wrappers triviales y código cosmético

| # | Archivo | Problema | Código actual | Qué hacer |
|---|---|---|---|---|
| 1 | `search/presentation/CoinSearchViewModel.kt` | `retryLoadSymbols()` es un wrapper que solo delega a `loadSymbols()` | `fun retryLoadSymbols() { loadSymbols() }` | Opción A: eliminarlo y que la UI llame directo a `loadSymbols()`. Opción B: agregarle lógica real (ej: límite de reintentos, delay progresivo) |
| 2 | `search/presentation/CoinSearchViewModel.kt` | `onCancelLoadSymbols()` asigna `loadSymbolsJob = null` al final, pero el Job se reemplaza en la próxima `loadSymbols()` de todos modos | `fun onCancelLoadSymbols() { loadSymbolsJob?.cancel(); _asyncAvailableSymbols.value = Loadable.Cancelled; loadSymbolsJob = null }` | El `loadSymbolsJob = null` es redundante. Se puede eliminar esa línea |

---

## Resumen de archivos a modificar

| Archivo | Issue | Prioridad |
|---|---|---|
| `core/di/CecoinDependencyInjector.kt` (+4-5 nuevos) | God Object | P1 |
| `search/presentation/CoinSearchViewModel.kt` | toggleFavorite traga errores | P1 |
| `chart/domain/usecase/GetHistoricalPricesUseCaseTest.kt` | Edge case faltante | P2 |
| `news/domain/usecase/GetCryptoNewsUseCaseTest.kt` | Edge cases faltantes | P2 |
| `search/domain/usecase/GetAvailableSymbolsUseCaseTest.kt` | Edge cases faltantes | P2 |
| `search/domain/usecase/ToggleFavoriteUseCaseTest.kt` | Edge case faltante | P2 |
| `search/domain/usecase/ObserveFavoritesUseCaseTest.kt` | Edge cases faltantes | P2 |
| `search/presentation/CoinSearchViewModelTest.kt` | Tests redundantes | P2 |
| `core/utils/TimeConstants.kt` | Código muerto | P3 |
| `chart/presentation/Fakes.kt` (commonTest) | Código muerto | P3 |
| `chart/data/repository/ChartRepositoryImplTest.kt` | Código muerto | P3 |
