# cecoin

Cryptocurrency portfolio tracker built with Kotlin Multiplatform + Compose Desktop.

## Architecture

Clean Architecture + MVVM with 4 layers:

- **Domain** — use cases, models, repository interfaces, error classification
- **Data** — repository implementations, API data sources (Binance, NewsAPI)
- **Presentation** — ViewModels, Compose UI, state management (`Loadable`, `Fallible`)
- **Core** — shared utilities, formatters, base types

## Project structure

```
cecoin/
├── desktopApp/          # JVM Desktop entry point
├── shared/
│   ├── src/
│   │   ├── commonMain/  # Shared multiplatform code
│   │   └── jvmMain/     # JVM-specific implementations
│   └── build.gradle.kts
├── informe-v2-17-06.md  # Development progress report (gitignored)
└── README.md
```

## Features

- Real-time price chart with granularity selection (M1, M5, M15, M30, H1, H6, D1)
- Cryptocurrency search with favorite symbols
- Crypto news feed with search filtering
- Favorite symbol management (persisted with DataStore)
- Dark-themed Compose UI

## Run

```bash
./gradlew :desktopApp:run
```

## Test

```bash
./gradlew :shared:allTests
```

161 tests across 22 test files — use cases, repositories, ViewModels, and UI utilities.
