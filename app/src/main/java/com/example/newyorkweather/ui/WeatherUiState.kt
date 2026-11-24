package com.example.newyorkweather.ui

import com.example.newyorkweather.data.WeatherResponse

/** Everything the weather screen needs to draw one frame. */
sealed interface WeatherUiState {

    /** A request is in flight. */
    data object Loading : WeatherUiState

    /** The server answered and the JSON was parsed into [weather]. */
    data class Success(
        val weather: WeatherResponse,
        /** Device clock time of the response, in epoch milliseconds. */
        val fetchedAt: Long,
    ) : WeatherUiState

    /** The request failed; [reason] says why and [code] is the HTTP status, if any. */
    data class Error(val reason: WeatherError, val code: Int? = null) : WeatherUiState
}

/** The failure scenarios the app knows how to explain to the user. */
enum class WeatherError {
    /** No key was configured in local.properties at build time. */
    MISSING_API_KEY,

    /** OpenWeatherMap rejected the key (HTTP 401). */
    INVALID_API_KEY,

    /** The city does not exist (HTTP 404). */
    CITY_NOT_FOUND,

    /** The device could not reach the server (no connectivity, timeout, DNS...). */
    NETWORK,

    /** The server answered, but with an error status. */
    SERVER,

    /** The response could not be parsed, or anything else went wrong. */
    UNEXPECTED,
}
