package com.example.newyorkweather.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newyorkweather.BuildConfig
import com.example.newyorkweather.data.WeatherApiClient
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException

/**
 * Owns the weather request and polls it while the screen is visible.
 *
 * The screen calls [startPolling] when it becomes visible and [stopPolling]
 * when it leaves the foreground, so the app never spends battery or API quota
 * in the background.
 */
class WeatherViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    /** Serializes requests so a retry tap and a scheduled poll never overlap. */
    private val refreshMutex = Mutex()

    private var pollingJob: Job? = null

    var city: String = CITY
        private set

    fun setCity(newCity: String) {
        val trimmed = newCity.trim()
        if (trimmed.isBlank() || trimmed == city) return
        city = trimmed
        _uiState.value = WeatherUiState.Loading
        viewModelScope.launch {
            refresh()
        }
    }

    /** Starts the polling loop. Safe to call repeatedly. */
    fun startPolling() {
        if (pollingJob?.isActive == true) return

        if (BuildConfig.OPENWEATHER_API_KEY.isBlank()) {
            _uiState.value = WeatherUiState.Error(WeatherError.MISSING_API_KEY)
            return
        }

        pollingJob = viewModelScope.launch {
            while (isActive) {
                refresh()
                delay(POLL_INTERVAL_MILLIS)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    /** One immediate request, used by the "Try again" button. */
    fun refreshNow() {
        viewModelScope.launch { refresh() }
    }

    private suspend fun refresh() = refreshMutex.withLock {
        val apiKey = BuildConfig.OPENWEATHER_API_KEY
        if (apiKey.isBlank()) {
            _uiState.value = WeatherUiState.Error(WeatherError.MISSING_API_KEY)
            return@withLock
        }

        // Show the spinner only when there is nothing to display yet: a poll
        // that runs every minute must not make the screen flicker.
        if (_uiState.value !is WeatherUiState.Success) {
            _uiState.value = WeatherUiState.Loading
        }

        try {
            val weather = WeatherApiClient.api.getCurrentWeather(
                city = city,
                apiKey = apiKey,
            )
            _uiState.value = WeatherUiState.Success(
                weather = weather,
                fetchedAt = System.currentTimeMillis(),
            )
        } catch (e: HttpException) {
            // The server answered with an error status (4xx / 5xx).
            val reason = when (e.code()) {
                401 -> WeatherError.INVALID_API_KEY
                404 -> WeatherError.CITY_NOT_FOUND
                else -> WeatherError.SERVER
            }
            keepLastDataOrShow(reason, e.code())
        } catch (e: IOException) {
            // No connection, DNS failure, timeout, connection reset...
            keepLastDataOrShow(WeatherError.NETWORK)
        } catch (e: JsonDataException) {
            // The JSON did not match the model classes.
            keepLastDataOrShow(WeatherError.UNEXPECTED)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            keepLastDataOrShow(WeatherError.UNEXPECTED)
        }
    }

    /**
     * A failed poll keeps the last good weather on screen instead of replacing
     * it with an error page; the "Updated" time stops advancing instead.
     */
    private fun keepLastDataOrShow(reason: WeatherError, code: Int? = null) {
        if (_uiState.value !is WeatherUiState.Success) {
            _uiState.value = WeatherUiState.Error(reason, code)
        }
    }

    companion object {
        const val CITY = "New York"

        /** One request per minute; the free tier allows 60 per minute. */
        const val POLL_INTERVAL_MILLIS = 60_000L
    }
}
