# Weather App

A modern, high-performance Android weather app built with Jetpack Compose and Material 3. It displays real-time weather conditions for **any city worldwide** using the [OpenWeatherMap](https://openweathermap.org/api) API.

## Features

- **Global Location Search**: Instantly search and switch between cities worldwide using the search action in the top app bar.
- **Real-Time Weather Data**: Displays current city name, temperature, main weather condition (e.g. Clear, Clouds, Rain), and detailed description (e.g. clear sky, broken clouds).
- **Vibrant Weather Icons**: Network-loaded high-resolution weather graphics via Glide (`@4x` resolution) paired with clear Material Vector icon fallbacks (e.g. bright golden sun for clear day, crescent moon for clear night).
- **Lifecycle-Aware Polling**: Auto-refreshes current weather every 60 seconds while the app is in the foreground and automatically pauses when stopped to conserve battery and API quota.
- **Robust Error Handling**: Handles network disconnections, invalid/missing API keys (HTTP 401), missing city (HTTP 404), and server failures with user-friendly retry actions.
- **Material 3 Design**: Supports edge-to-edge layout, dark teal theme styling, and Compose previews.

## Tech Stack

| Layer | Technology |
|---|---|
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | ViewModel + `StateFlow` + Coroutines |
| **Networking** | Retrofit 2.11 + Moshi 1.15 + OkHttp 4.12 |
| **Image Loading** | Glide 4.16 |
| **Build & Tooling** | Gradle (Kotlin DSL), AGP 9.5.2, Kotlin 2.2 |

* **Target SDK**: 37 | **Min SDK**: 24

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/Gerolaga/Gerolaga_Weather.git
cd Weather-App
```

### 2. Requirements

- Android Studio (Ladybug or newer) with JDK 17+
- An OpenWeatherMap API key ([Get a free key here](https://home.openweathermap.org/api_keys))

### 3. Configure the API Key

The API key is kept secure and out of version control. Add your key to `local.properties` in the project root:

```properties
OPENWEATHER_API_KEY=your-actual-api-key-here
```

`local.properties` is listed in `.gitignore`. Alternatively, export `OPENWEATHER_API_KEY` as an environment variable before building:

```bash
# Windows (PowerShell)
$env:OPENWEATHER_API_KEY="your-actual-api-key-here"

# macOS / Linux
export OPENWEATHER_API_KEY="your-actual-api-key-here"
```

### 4. Build and Run

```bash
# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

Or open the project in Android Studio and click **Run** (Shift+F10).

### 5. Run Unit Tests

```bash
# Windows
gradlew.bat test

# macOS / Linux
./gradlew test
```

## Project Layout

```
app/src/main/java/com/example/newyorkweather/
├── MainActivity.kt          # Edge-to-edge Activity & foreground polling setup
├── data/
│   ├── WeatherApi.kt        # Retrofit interface & WeatherApiClient singleton
│   └── WeatherResponse.kt   # Moshi JSON data models (WeatherResponse, Weather, Main, Coord)
└── ui/
    ├── WeatherScreen.kt     # Compose screen UI, search dialog & WeatherIcon
    ├── WeatherUiState.kt    # UI State definition & WeatherError enum
    ├── WeatherViewModel.kt  # StateFlow management, city search & polling loop
    └── theme/               # Material 3 colors, typography & theme definitions
```

## Security & Secrets

- API keys are injected via `local.properties` or environment variables at build time and injected into `BuildConfig.OPENWEATHER_API_KEY`.
- Never commit `local.properties`, `.env` files, or signing keys to Git.
- If an API key is accidentally exposed, revoke it immediately at [home.openweathermap.org/api_keys](https://home.openweathermap.org/api_keys).
