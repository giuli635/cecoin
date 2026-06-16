# Análisis de Código — CECoin

**Fecha:** 16/06/2026
**Propósito:** Code Review del proyecto CECoin (Kotlin Multiplatform + Compose Desktop) para alinear con Clean Architecture, MVVM, SOLID y cobertura de tests.

---

## Índice

1. [Arquitectura — Clean Architecture + MVVM](#1-arquitectura--clean-architecture--mvvm)
2. [Clean Code + SOLID](#2-clean-code--solid)
3. [Tests Unitarios](#3-tests-unitarios)
4. [Manejo de Estados en UI (Loading/Success/Error)](#4-manejo-de-estados-en-ui-loadingsuccesserror)
5. [Discusiones Técnicas](#5-discusiones-técnicas)
   - [5.1 commonMain vs jvmMain: el problema de java.io.File](#51-commonmain-vs-jvmmain-el-problema-de-javaiofile)
   - [5.2 Ktor vs Retrofit](#52-ktor-vs-retrofit)
   - [5.3 HttpClient único vía DI](#53-httpclient-único-vía-di)
   - [5.4 ¿Por qué tests para clases no listadas por el profesor?](#54-por-qué-tests-para-clases-no-listadas-por-el-profesor)
   - [5.5 Magic Numbers/Strings detectados](#55-magic-numbersstrings-detectados)
   - [5.6 Archivos extensos — fragmentación sugerida](#56-archivos-extensos--fragmentación-sugerida)
6. [TODO List — Prompts Atómicos para Trabajo en Paralelo](#6-todo-list--prompts-atómicos-para-trabajo-en-paralelo)

---

## 1. Arquitectura — Clean Architecture + MVVM

### Estructura actual

```
shared/src/commonMain/kotlin/dyds/crypto/cecoin/
├── domain/
│   ├── model/           → CryptoSymbol, NewsArticle, PricePoint, TradePrice
│   ├── repository/      → CryptoSymbolRepository, FavoriteRepository,
│   │                       NewsRepository, TradePriceRepository (interfaces)
│   └── usecase/         → GetAvailableSymbols, GetCryptoNews, GetHistoricalPrices,
│                           ObserveFavorites, ObserveTradePrices, ToggleFavorite
├── data/
│   ├── local/           → FavoriteDataSource, DataStoreFavoriteDataSource
│   ├── remote/          → CoinHistoricalDataSource, CoinListDataSource,
│   │                       CoinPriceDataSource, NewsApiDataSource,
│   │                       BinanceCoinHistoricalDataSource, BinanceCoinListDataSource,
│   │                       BinanceCoinPriceDataSource, NewsApiRestDataSource
│   └── repository/      → CecoinRepositoryImpl, FavoriteRepositoryImpl, NewsRepositoryImpl
├── presentation/
│   ├── chart/           → ChartScreen, ChartScreenViewModel, ChartDataController,
│   │                       GranularityStateHolder, component/, model/, util/
│   ├── news/            → NewsScreen, NewsViewModel, NewsUiState, component/
│   ├── search/          → CoinSearchScreen, CoinSearchViewModel, CoinSearchUiState,
│   │                       component/, util/
│   └── utils/           → buildAsyncComposable, buildAsyncStreamComposable,
│                           buildCancellableComposable, buildFallibleComposable,
│                           buildLoadableComposable
├── di/                  → CecoinDependencyInjector
└── utils/               → AppError, GenericStates (Loadable, Fallible)
```

### ✅ Aciertos

| Aspecto | Estado | Detalle |
|---------|--------|---------|
| Separación de capas | ✅ | `domain/` → `data/` → `presentation/` → `di/` claramente diferenciadas |
| Dirección de dependencias | ✅ | `presentation` → `domain` ← `data`. Domain no conoce a nadie |
| ViewModels inyectan Use Cases | ✅ | No dependen directamente de repositorios ni de data sources |
| Módulo DI centralizado | ✅ | `CecoinDependencyInjector` como object con factoría manual |
| Interfaces de repositorio en domain | ✅ | `CryptoSymbolRepository`, `TradePriceRepository`, etc. |

### ❌ Hallazgos

| ID | Severidad | Archivo | Línea(s) | Problema |
|----|-----------|---------|----------|----------|
| A1 | **Critical** | `data/local/DataStoreFavoriteDataSource.kt` | 28-33 | Usa `java.io.File` y `System.getProperty` en `commonMain` (ver [§5.1](#51-commonmain-vs-jvmmain-el-problema-de-javaiofile)) |
| A2 | **High** | `data/repository/CecoinRepositoryImpl.kt` | 18-19 | Implementa **dos interfaces** (`CryptoSymbolRepository` + `TradePriceRepository`). Viola ISP y SRP. Debe dividirse |
| A3 | **Medium** | `data/remote/BinanceCoinPriceDataSource.kt` | — | Cada data source crea su propio `HttpClient()`. Debería compartirse uno solo vía DI (ver [§5.3](#53-httpclient-único-vía-di)) |
| A4 | **Medium** | `data/remote/BinanceCoinListDataSource.kt` | — | Mismo problema que A3 |
| A5 | **Medium** | `data/remote/BinanceCoinHistoricalDataSource.kt` | — | Mismo problema que A3 |
| A6 | **Medium** | `data/remote/NewsApiRestDataSource.kt` | — | Mismo problema que A3 |
| A7 | **Low** | `presentation/chart/ChartScreen.kt` | 47 | Usa `String.format()` (API JVM estándar) en `commonMain`. No es KMP-compatible |

---

## 2. Clean Code + SOLID

### ✅ Aciertos

- Uso extensivo de `val` sobre `var` (inmutabilidad favorecida)
- Nombres de clases y funciones expresivos
- Constantes con nombre en casi todos los archivos (`private const val`)
- Funciones cortas en general (promedio ~15-20 líneas)
- `data class` para todos los modelos de dominio
- Uso de `sealed class` para `Loadable` y `Fallible`
- Separación clara de responsabilidades entre ViewModel, repository, data source

### ❌ Violaciones SOLID

| ID | Principio | Archivo | Detalle |
|----|-----------|---------|---------|
| S1 | **SRP/ISP** | `CecoinRepositoryImpl` | Implementa `CryptoSymbolRepository` (símbolos disponibles) y `TradePriceRepository` (precios históricos + streaming). Dos razones de cambio distintas. Solución: crear `CryptoSymbolRepositoryImpl` y `TradePriceRepositoryImpl` por separado |
| S2 | **SRP** | `ChartDataController` | Gestiona: (a) acumulación de puntos, (b) lifecycle del stream, (c) lógica de retry, (d) emisión de estado. Al menos 3 responsabilidades mezcladas |
| S3 | **SRP** | `ChartScreenViewModel` | Maneja: (a) carga de históricos, (b) creación del controller, (c) lifecycle del stream. Delegar creación del controller a DI o factory externa |

### ❌ Nombres ambiguos o genéricos

| Archivo | Detalle |
|---------|---------|
| `presentation/chart/ChartScreen.kt:36` | `ChartContent` como función privada — correcto para encapsulación, pero nombre genérico |
| `presentation/search/component/CoinItem.kt:45` | Uso de `\u2605` / `\u2606` sin constante con nombre |

### ❌ Código muerto / imports sin usar

No se detectaron imports sin usar significativos. Todos los archivos se referencian en al menos un lugar.

### ❌ Fragmentación sugerida (ver [§5.6](#56-archivos-extensos--fragmentación-sugerida))

---

## 3. Tests Unitarios

### Cobertura vs Requerimiento del Profesor

| Requerimiento (ítem 5.3) | Estado | Archivos de test |
|--------------------------|--------|-----------------|
| ViewModels (todos) | ✅ | `CoinSearchViewModelTest`, `NewsViewModelTest`, `ChartScreenViewModelTest` |
| Use Cases (todos) | ✅ | `GetAvailableSymbolsUseCaseTest`, `GetCryptoNewsUseCaseTest`, `GetHistoricalPricesUseCaseTest`, `ObserveFavoritesUseCaseTest`, `ObserveTradePricesUseCaseTest`, `ToggleFavoriteUseCaseTest` |
| Repositorios (implementación) | ✅ | `CecoinRepositoryImplTest`, `FavoriteRepositoryImplTest`, `NewsRepositoryImplTest` |
| Data Sources locales | ✅ | `DataStoreFavoriteDataSourceTest` |
| Broker / Combinación de APIs | ✅ (parcial) | `ChartDataControllerTest` |

### ✅ Puntos fuertes de los tests

- Uso de `Fakes` en lugar de Mocks (simples, predecibles)
- Tests con `runTest` de Kotlin Coroutines Test
- `ChartScreenViewModelTest` con cobertura muy completa (263 líneas, 16 tests)
- `ChartDataControllerTest` cubre seed, streaming, cancelación, errores, retry
- `CoinSearchViewModelTest` cubre filtros, favoritos, carga, error, cancelación
- Separación de Fakes por capa (`data/Fakes.kt` y `domain/Fakes.kt`)

### ❌ Edge Cases No Cubiertos

| Archivo de test | Edge case faltante |
|----------------|-------------------|
| `GetAvailableSymbolsUseCaseTest` | Repositorio lanza excepción |
| `GetCryptoNewsUseCaseTest` | Repositorio lanza excepción |
| `ToggleFavoriteUseCaseTest` | Doble toggle (toggle → toggle → estado original) |
| `ObserveFavoritesUseCaseTest` | Flow vacío, múltiples emisiones |
| `ObserveTradePricesUseCaseTest` | Flow vacío, múltiples emisiones |
| `FavoriteRepositoryImplTest` | Símbolo vacío (`""`), caracteres especiales |
| `CecoinRepositoryImplTest` | Error del data source remoto, símbolos con caracteres especiales |
| `DataStoreFavoriteDataSourceTest` | Concurrencia en toggle, símbolos con prefijo/espacios |
| `NewsRepositoryImplTest` | `NewsArticle` con campos nulos |

### ❌ Tests Sugeridos Adicionales

> **Nota:** Estos tests van MÁS ALLÁ del requerimiento del profesor. Son recomendaciones para reducir riesgo. Evaluar si vale la pena según tiempo disponible. Ver [§5.4](#54-por-qué-tests-para-clases-no-listadas-por-el-profesor).

| Componente | Riesgo | Justificación |
|------------|--------|---------------|
| `SymbolFilter.filterBy()` | Medio | Lógica de filtrado con 3 parámetros (query, filterMode, favorites). Si falla, el search no funciona correctamente |
| `PriceBucketer.foldTradePrice()` | Bajo | Probado indirectamente; aceptable sin test dedicado |
| `RangeProvider.*` (`computeChartYRange`, `niceStep`, etc.) | Medio | Lógica matemática con múltiples ramas condicionales. Propensa a errores esquina (edge cases con rangos negativos, cero, etc.) |
| `ChartFormatter.*` (`priceStr`, `timeFormatter`) | Medio | Formateo manual de precios y timestamps. Si hay un bug, el chart muestra datos incorrectos |
| `GranularityStateHolder` | Bajo | Ya testeado (`GranularityStateHolderTest`) |

---

## 4. Manejo de Estados en UI (Loading/Success/Error)

### ✅ Aciertos

- Sistema unificado: `AsyncResult<T> = Loadable<Fallible<T>>`
- `buildLoadableComposable` → `CircularProgressIndicator` en Loading
- `buildFallibleComposable` → mensaje de error + botón "Reintentar"
- `buildCancellableComposable` → botón "Cancelar" durante carga
- `buildAsyncComposable` compone los tres anteriores
- Todos los ViewModels emiten `Loading` antes de comenzar operaciones
- `NewsScreen` y `CoinSearchScreen` deshabilitan inputs durante loading
- `GranularityStateHolder` evita emisión duplicada de mismo valor

### ❌ Hallazgos

| ID | Severidad | Archivo | Problema |
|----|-----------|---------|----------|
| U1 | **High** | `chart/ChartScreen.kt:83` | `buildAsyncStreamComposable` recibe `onBack` como `onCancel`. Presionar "Cancelar" durante la carga navega hacia atrás (vuelve al listado) en vez de solo cancelar el stream. Es por esto que NUNCA veían el falso error de `CancellationException` en el chart: al navegar hacia atrás, la pantalla se destruye antes de que el error se renderice. En cambio, en SearchScreen y NewsScreen cancelar NO navega — se queda en la misma pantalla, por eso ahí SÍ aparecía el mensaje "StandaloneCoroutine was cancelled" |
| U2 | **Medium** | `chart/ChartScreen.kt:38-48` | `lastOrNull()?.price ?: 0.0` trata precio = 0.0 como "sin datos". Si una crypto vale exactamente 0 (stablecoin colapsada, por ejemplo), no se muestra el precio. Mejor usar `null` o un flag explícito |
| U3 | **Medium** | `search/CoinSearchScreen.kt:97-98` | Cuando no hay query de búsqueda (`searchQuery` vacío) y la lista de monedas está vacía, no se muestra ningún mensaje. Solo hay mensaje de "no se encontraron criptos" cuando hay query. Debería mostrar "No hay criptomonedas disponibles" |
| U4 | **Low** | `chart/ChartScreen.kt:78-80` | `LaunchedEffect(granularity)` se re-ejecuta al cambiar granularidad, lo pisa el `GranularityStateHolder` que no emite si el valor no cambia. Correcto por ahora, pero frágil si alguien cambia `GranularityStateHolder.set()` |
| U5 | **Low** | `utils/buildAsyncStreamComposable.kt:22-30` | Si el `Flow` está vacío (no emite nunca), el estado se queda en `Loading` para siempre. No hay timeout ni estado empty |

---

## 5. Discusiones Técnicas

### 5.1 commonMain vs jvmMain: el problema de `java.io.File`

#### ¿Qué pasa actualmente?

`DataStoreFavoriteDataSource.kt` en `commonMain` usa:

```kotlin
import java.io.File
// ...
val file = File(System.getProperty("user.home"), ".cecoin/favorites.preferences_pb")
```

#### ¿Por qué compila?

`shared/build.gradle.kts` define:

```kotlin
kotlin {
    jvm()
}
```

El proyecto solo compila a JVM. No hay targets Android, iOS, JS, etc. Entonces `commonMain` se compila con el classpath de JVM y las APIs de `java.io` están disponibles.

#### ¿Es un problema?

Depende de las intenciones futuras:

| Escenario | Impacto |
|-----------|---------|
| **Solo JVM para siempre** | Bajo. Compila y funciona. Pero esconde una dependencia JVM en `commonMain`, lo que va contra el espíritu de KMP. Si mañana alguien agrega `androidTarget()`, el código de `commonMain` no compilará |
| **Posible multiplataforma a futuro** | Alto. Habría que refactorizar todo el data source |

#### Soluciones recomendadas (ordenadas de mejor a peor)

**Opción A (recomendada) — `expect/actual`**

```
commonMain/
  data/local/
    FavoriteDataSource.kt          (interface, ya existe)
    DataStoreFavoriteDataSource.kt  (SÓLO la lógica común, sin File)

jvmMain/
  data/local/
    JvmDataStoreFactory.kt         (creación del DataStore con File)
```

Ejemplo:

```kotlin
// commonMain — expect declaration
expect fun createFavoriteDataStore(): DataStore<Preferences>

// jvmMain — actual implementation
actual fun createFavoriteDataStore(): DataStore<Preferences> {
    val file = File(System.getProperty("user.home"), ".cecoin/favorites.preferences_pb")
    file.parentFile.mkdirs()
    return PreferenceDataStoreFactory.create { file }
}
```

**Opción B (mínima) — mover FactoryMethod al injector**

Mover la creación del `DataStore` a `CecoinDependencyInjector` (o a una clase factory separada) y pasarlo como parámetro al constructor de `DataStoreFavoriteDataSource`. El injector vive en `commonMain` pero igual depende de JVM indirectamente. No soluciona el problema de raíz pero centraliza la dependencia.

**Opción C (no recomendada) — dejar como está**

Compila hoy, pero crea deuda técnica. Si el proyecto queda para futuros cuatrimestres, alguien va a pinchar con este tema.

---

### 5.2 Ktor vs Retrofit

El requerimiento del profesor dice: *"Data: Implementación de repositorios, fuentes remotas (Retrofit) y local (Room o DataStore)"*. El proyecto usa **Ktor Client** en lugar de Retrofit.

Además, en un proyecto anterior de la misma materia ya trabajaron con Ktor. Es el cliente HTTP que conocen y dominan. Cambiar a Retrofit ahora sería un riesgo innecesario.

| Aspecto | Retrofit | Ktor Client |
|---------|----------|-------------|
| Lenguaje | Java (con soporte Kotlin via adapters) | Kotlin nativo |
| Multiplataforma | Android + JVM | KMP (JVM, Android, iOS, JS, Native) |
| WebSockets | Requiere OkHttp + adapter | Nativo (`ktor-client-websockets`) |
| DSL | Anotaciones (`@GET`, `@POST`, etc.) | Programático (DSL Kotlin) |
| Serialización | Gson / Moshi / Kotlinx | Kotlinx Serialization |
| Experiencia previa | No usaron en la materia | ✅ Ya lo usaron en proyectos anteriores |

**¿Está mal usar Ktor?** No. De hecho, es la elección más coherente para un proyecto Kotlin Multiplatform. El profesor mencionó Retrofit como ejemplo de cliente HTTP, no como requisito excluyente. Ktor cumple el mismo rol. Además, el proyecto necesita WebSockets (para precios en vivo), y la integración de Ktor con WebSockets es mucho más limpia que con Retrofit+OkHttp.

**Veredicto:** ✅ Quédense con Ktor. No hay necesidad de migrar a Retrofit. Ya tienen experiencia con Ktor de proyectos anteriores, es el estándar del grupo.

---

### 5.3 HttpClient único vía DI

#### Situación actual

Cada data source crea su propio `HttpClient`:

```
BinanceCoinHistoricalDataSource → HttpClient()
BinanceCoinListDataSource       → HttpClient()
BinanceCoinPriceDataSource      → HttpClient()  (con WebSockets)
NewsApiRestDataSource           → HttpClient()
```

#### ¿Por qué es problemático?

1. **Conexiones duplicadas**: Cada `HttpClient` mantiene su propio pool de conexiones, hilos, y buffers
2. **Configuración inconsistente**: Si mañana hay que agregar un header de autenticación o un timeout global, hay que tocar 4 archivos
3. **Resource leak**: Si olvidan llamar a `close()` en alguno, quedan hilos colgados

#### Solución: crear un único HttpClient en el DI

```kotlin
// di/CecoinDependencyInjector.kt (o un modulo HttpClientModule)

private val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(WebSockets)
    defaultRequest {
        url {
            // config global si aplica
        }
    }
}
```

Luego se pasa a cada data source:

```kotlin
class BinanceCoinHistoricalDataSource(
    private val httpClient: HttpClient,
) : CoinHistoricalDataSource { ... }

class BinanceCoinListDataSource(
    private val httpClient: HttpClient,
) : CoinListDataSource { ... }

class BinanceCoinPriceDataSource(
    private val httpClient: HttpClient,
) : CoinPriceDataSource { ... }

class NewsApiRestDataSource(
    private val httpClient: HttpClient,
) : NewsApiDataSource { ... }
```

Y en el DI:

```kotlin
private val coinHistoricalSource = BinanceCoinHistoricalDataSource(httpClient)
private val coinListDataSource = BinanceCoinListDataSource(httpClient)
// etc.
```

**Ventajas:** pool único, config centralizada, close() único, testeabilidad (se puede inyectar un mock/fake HttpClient en tests).

---

### 5.4 ¿Por qué tests para clases no listadas por el profesor?

El requerimiento 5.3 dice:

> *Deben implementar tests unitarios para las siguientes clases, con cobertura de los happy paths y edge cases relevantes: ViewModels (todos), Use Cases (todos), Implementación del repositorio, Data Sources locales, Broker / lógica de combinación de datos entre APIs (si aplica)*

En el análisis sugerí tests adicionales para `SymbolFilter.filterBy()`, `RangeProvider.*`, `ChartFormatter.*`. La razón no es porque el profesor los pida, sino porque:

1. **Contienen lógica condicional** con ramas que pueden fallar
2. **Son funciones públicas o internas** que otros componentes consumen
3. **Un bug en ellas no se detectaría fácilmente** ni en UI ni en tests indirectos

Dicho esto, **si el profesor solo va a evaluar las categorías listadas, prioricen completar bien esas antes de agregar tests extra**. Mi recomendación ordenada por impacto:

1. ✅ Completar los edge cases faltantes de los tests existentes (ver §3)
2. ⏳ Si sobra tiempo: `SymbolFilter` (fácil, rápido, 14 líneas de código)
3. ⏳ Si sobra tiempo: `RangeProvider.computeChartYRange` (mediano, varias ramas)
4. ❌ Si no sobra tiempo: omitir `ChartFormatter`, `VicoChartModelBuilder`, `PriceBucketer` (probados indirectamente)

---

### 5.5 Magic Numbers/Strings Detectados

Archivos que revisamos: todos los `.kt` en `commonMain`.

| Estado | Archivo | Detalle |
|--------|---------|---------|
| ✅ OK | `ChartDataController.kt` | `RETRY_DELAY_MS = 1000L`, `MAX_STREAM_RETRIES = 3`, `STREAM_FAILED = "..."` |
| ✅ OK | `NewsApiRestDataSource.kt` | `NEWSAPI_KEY`, `NEWSAPI_URL`, `NEWS_QUERY`, etc. |
| ✅ OK | `ChartScreen.kt` | `BACK_BUTTON`, `USD_LABEL` |
| ✅ OK | `CoinSearchScreen.kt` | `SEARCH_TITLE`, `SEARCH_LABEL`, etc. |
| ✅ OK | `NewsScreen.kt` | `NEWS_TITLE`, `SEARCH_LABEL`, etc. |
| ✅ OK | `CoinSearchViewModel.kt` | `FAILED_TO_LOAD_SYMBOLS` |
| ✅ OK | `ChartScreenViewModel.kt` | `DEFAULT_HISTORICAL_LIMIT = 200`, `HISTORICAL_FAILED` |
| ✅ OK | `CecoinRepositoryImpl.kt` | `DefaultSymbol = "BTCUSDT"` |
| ✅ OK | `BinanceCoinPriceDataSource.kt` | `BASE_URLS`, `WEBSOCKET_CONNECT_ERROR`, `STREAM_SUFFIX` |
| ⚠️ **FIX** | `ChartFormatter.kt:12-14` | `1000`, `60`, `3600`, `24` usados directamente. Extraer a constantes: `MILLIS_IN_SECOND = 1000`, `SECONDS_IN_MINUTE = 60`, `SECONDS_IN_HOUR = 3600`, `HOURS_IN_DAY = 24` |
| ⚠️ **FIX** | `RangeProvider.kt:87` | `1000.0` usado directamente. Extraer a `MILLIS_IN_SECOND_DOUBLE = 1000.0` |
| ⚠️ **FIX** | `ChartScreen.kt:47` | `"%,.2f"` es un magic string de formato. Extraer a `PRICE_FORMAT = "%,.2f"` |

---

### 5.6 Archivos Extensos — Fragmentación Sugerida

Regla práctica: archivos > 100 líneas en `commonMain` merecen revisión.

| Archivo | Líneas | Evaluación | Sugerencia |
|---------|--------|------------|------------|
| `presentation/chart/component/PriceChart.kt` | 143 | **Fragmentable** | Separar componentes de marker, axis labels, y scroll logic en archivos propios. El `CartesianChartHost` con toda su configuración ocupa ~70 líneas. Se podría extraer a un `ChartHost.kt` o `ChartConfig.kt` |
| `presentation/Navigation.kt` | 119 | **Fragmentable** | Separar cada `NavGraphBuilder` en su propio archivo: `HomeNavigation.kt`, `NewsNavigation.kt`, `DetailNavigation.kt`. El `Navigation.kt` quedaría solo con el `NavHost` principal y la lógica de tabs |
| `presentation/chart/ChartScreen.kt` | 114 | Aceptable | Es una screen composable con toda su lógica visual. Es normal en Compose que una screen tenga 100-150 líneas. Considerar extraer `ChartContent` y el `Row` superior si se reutilizan |
| `presentation/search/CoinSearchScreen.kt` | 110 | Aceptable | Misma lógica que ChartScreen. Normal para Compose |
| `di/CecoinDependencyInjector.kt` | 90 | Aceptable | Podría separarse en `NetworkModule.kt`, `DatabaseModule.kt`, `ViewModelModule.kt` si crece, pero por ahora 90 líneas está bien |
| `presentation/chart/util/RangeProvider.kt` | 85 | Aceptable | Contiene varias funciones helper (`computeChartYRange`, `computeChartXRange`, `niceStep`, `niceTimeStep`). Podría separarse en `YRangeProvider.kt` y `XRangeProvider.kt` si se quiere |

**Conclusión:** `PriceChart.kt` (143 líneas) y `Navigation.kt` (119 líneas) son los candidatos principales para fragmentación. El resto están en rango aceptable para Compose Desktop.

---

## 6. TODO List — Prompts Atómicos para Trabajo en Paralelo

Cada tarea a continuación es independiente y puede asignarse a una persona distinta.

---

### Tarea 1: Dividir CecoinRepositoryImpl en dos clases

**Archivos a modificar:**
- `data/repository/CecoinRepositoryImpl.kt` — eliminar
- `data/repository/CryptoSymbolRepositoryImpl.kt` — crear (solo `CryptoSymbolRepository`)
- `data/repository/TradePriceRepositoryImpl.kt` — crear (solo `TradePriceRepository`)
- `data/remote/CoinHistoricalDataSource.kt` — sin cambios
- `data/remote/CoinListDataSource.kt` — sin cambios
- `data/remote/CoinPriceDataSource.kt` — sin cambios
- `di/CecoinDependencyInjector.kt` — crear dos instancias separadas
- `data/Fakes.kt` — crear fakes separados si es necesario

**Prompt para el desarrollador:**
```
Dividí CecoinRepositoryImpl en dos clases separadas: CryptoSymbolRepositoryImpl (implementa CryptoSymbolRepository) y TradePriceRepositoryImpl (implementa TradePriceRepository). Cada una recibe solo los data sources que necesita. Actualizá CecoinDependencyInjector para crear ambas instancias. Ajustá los tests y fakes según corresponda y verficá que compila.
```

---

### Tarea 2: Mover DataStoreFavoriteDataSource a jvmMain con expect/actual

**Archivos a modificar:**
- `data/local/FavoriteDataSource.kt` — sin cambios (se queda en commonMain)
- `data/local/DataStoreFavoriteDataSource.kt` — mover a commonMain sin la lógica de File
- `data/local/DataStoreFavoriteDataSource.kt` — en jvmMain, SOLO la factoría con File
- `di/CecoinDependencyInjector.kt` — ajustar creación de FavoriteDataSource

**Prompt para el desarrollador:**
```
Implementá el patrón expect/actual para la creación del DataStore de favoritos. En commonMain definí `expect fun createFavoriteDataStore(): DataStore<Preferences>`. En jvmMain creá la implementación actual que usa java.io.File y System.getProperty. DataStoreFavoriteDataSource debe recibir el DataStore por constructor. Si el proyecto solo compila a JVM, esto es preventivo pero ordena la arquitectura.
```

---

### Tarea 3: Centralizar HttpClient en CecoinDependencyInjector

**Archivos a modificar:**
- `data/remote/BinanceCoinHistoricalDataSource.kt` — recibir HttpClient por constructor
- `data/remote/BinanceCoinListDataSource.kt` — recibir HttpClient por constructor
- `data/remote/BinanceCoinPriceDataSource.kt` — recibir HttpClient por constructor
- `data/remote/NewsApiRestDataSource.kt` — recibir HttpClient por constructor
- `di/CecoinDependencyInjector.kt` — crear un solo HttpClient, pasarlo a cada data source

**Prompt para el desarrollador:**
```
Creá una única instancia de HttpClient en CecoinDependencyInjector con los plugins ContentNegotiation (JSON) y WebSockets. Modificá los 4 data sources remotos para recibir HttpClient por constructor. Eliminá los HttpClient() duplicados. Verificá que dispose() cierre el único HttpClient. No olvides actualizar también Fakes.kt en test.
```

---

### Tarea 4: Fragmentar Navigation.kt

**Archivos a modificar:**
- `presentation/Navigation.kt` — dejar solo lógica de tabs + NavHost
- `presentation/HomeNavigation.kt` — crear con `homeDestination()`
- `presentation/NewsNavigation.kt` — crear con `newsDestination()`
- `presentation/DetailNavigation.kt` — crear con `detailDestination()`

**Prompt para el desarrollador:**
```
Fragmentá Navigation.kt separando cada NavGraphBuilder en su propio archivo. homeDestination() → HomeNavigation.kt, newsDestination() → NewsNavigation.kt, detailDestination() → DetailNavigation.kt. Navigation.kt debe quedar solo con las funciones Navigation() y los @Serializable routes. Mantené todos los imports necesarios en cada archivo.
```

---

### Tarea 5: Fragmentar PriceChart.kt

**Archivos a modificar:**
- `presentation/chart/component/PriceChart.kt` — dejar solo el @Composable PriceChart
- `presentation/chart/component/ChartMarker.kt` — crear con markerIndicator y marker config
- `presentation/chart/component/ChartAxes.kt` — crear con startAxis y bottomAxis
- `presentation/chart/component/ChartScrollConfig.kt` — crear con scrollState y zoomState

**Prompt para el desarrollador:**
```
Fragmentá PriceChart.kt separando la configuración del marker, ejes, y scroll/zoom en archivos independientes. PriceChart.kt debe quedar solo con el @Composable principal. Cada sub-componente recibe los mismos parámetros que antes. Verificá que la UI del chart se comporte exactamente igual.
```

---

### Tarea 6: Desacoplar onCancel de onBack en ChartScreen

**Archivos a modificar:**
- `presentation/chart/ChartScreen.kt` — cambiar `onCancel = onBack` por `onCancel = { viewModel.cancel() }` en el `buildAsyncStreamComposable`

**Prompt para el desarrollador:**
```
En ChartScreen.kt, el buildAsyncStreamComposable recibe onBack como parámetro onCancel. Esto hace que presionar "Cancelar" durante la carga navegue hacia atrás en vez de solo cancelar el stream. Cambiá onCancel por una lambda que llame a viewModel.cancel(). La navegación atrás debe seguir siendo manejada exclusivamente por el botón "Atrás".
```

---

### Tarea 7: Agregar empty state en CoinSearchScreen para lista vacía

**Archivos a modificar:**
- `presentation/search/CoinSearchScreen.kt` — agregar mensaje cuando lista vacía sin query

**Prompt para el desarrollador:**
```
En CoinSearchScreen, cuando searchQuery está vacío y la lista de símbolos también está vacía (sin error), mostrá el mensaje "No hay criptomonedas disponibles" en lugar de no mostrar nada. Reutilizá el mismo estilo del mensaje existente para "No se encontraron criptos con '...'".
```

---

### Tarea 8: Extraer constantes en ChartFormatter.kt y RangeProvider.kt

**Archivos a modificar:**
- `presentation/chart/util/ChartFormatter.kt` — extraer `1000`, `60`, `3600`, `24`
- `presentation/chart/util/RangeProvider.kt` — extraer `1000.0`

**Prompt para el desarrollador:**
```
Reemplazá los números mágicos en ChartFormatter.kt y RangeProvider.kt con constantes con nombre. Ejemplo: val ms / 1000 → val ms / MILLIS_IN_SECOND (con private const val MILLIS_IN_SECOND = 1000). Hacé lo mismo para 60, 3600, 24, y 1000.0. Usá las constantes ya existentes en RangeProvider como referencia de estilo.
```

---

### Tarea 9: Extraer magic string de formato en ChartScreen.kt

**Archivos a modificar:**
- `presentation/chart/ChartScreen.kt` — extraer `"%,.2f"` a constante

**Prompt para el desarrollador:**
```
Extraé el string de formato "%,.2f" en ChartScreen.kt a una constante privada con nombre, por ejemplo: private const val PRICE_FORMAT = "%,.2f". Usala en String.format(PRICE_FORMAT, lastPrice).
```

---

### Tarea 10: Agregar edge cases faltantes en tests existentes

**Archivos a modificar:**
- `domain/usecase/GetAvailableSymbolsUseCaseTest.kt` — agregar test de excepción
- `domain/usecase/GetCryptoNewsUseCaseTest.kt` — agregar test de excepción
- `domain/usecase/ToggleFavoriteUseCaseTest.kt` — agregar test doble toggle
- `data/repository/FavoriteRepositoryImplTest.kt` — agregar símbolo vacío
- `data/repository/CecoinRepositoryImplTest.kt` — agregar error de data source remoto
- `data/local/DataStoreFavoriteDataSourceTest.kt` — agregar símbolos con espacios

**Prompt para el desarrollador:**
```
Completá los edge cases faltantes en los tests existentes:
1. GetAvailableSymbolsUseCaseTest: test que cuando el repositorio lanza excepción, la excepción se propaga
2. GetCryptoNewsUseCaseTest: idem
3. ToggleFavoriteUseCaseTest: toggle("X") dos veces → estado original
4. FavoriteRepositoryImplTest: toggleFavorite("") no debe crashear
5. CecoinRepositoryImplTest: getHistoricalPrices con error del source debe propagar excepción
6. DataStoreFavoriteDataSourceTest: toggle con símbolo "  BTC  " debe funcionar (trim automático del repositorio)
Usá runTest y las Fakes existentes.
```

---

### Tarea 11: Extraer precio = 0.0 como caso válido en ChartScreen

**Archivos a modificar:**
- `presentation/chart/ChartScreen.kt` — manejar precio = 0.0 sin ocultarlo

**Prompt para el desarrollador:**
```
En ChartScreen.kt, ChartContent oculta el precio cuando lastPrice <= 0.0 porque usa lastOrNull()?.price ?: 0.0 y chequea lastPrice > 0.0. Cambiá la lógica para que null signifique "sin datos" (no mostrar precio) y 0.0 sea un precio válido (mostrar $0.00). Usá un nullable Double en lugar de 0.0 como default.
```

---

### Tarea 12: Agregar timeout en buildAsyncStreamComposable para evitar Loading infinito

**Archivos a modificar:**
- `presentation/utils/buildAsyncStreamComposable.kt` — agregar timeout configurable

**Prompt para el desarrollador:**
```
Agregá un timeout opcional (por defecto 30 segundos) en buildAsyncStreamComposable. Si el Flow no emite ningún valor en ese tiempo, emití un estado Fallible.Failed con "La transmisión de datos no respondió. Intente nuevamente." El timeout debe cancelarse si el Flow emite algo antes.
```
