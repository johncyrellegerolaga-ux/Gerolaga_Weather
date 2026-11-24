package com.example.newyorkweather.data

import com.squareup.moshi.Json

/**
 * Local model of the JSON returned by
 * `https://api.openweathermap.org/data/2.5/weather`.
 *
 * Moshi maps JSON keys to these properties by name, and ignores any key that
 * is not declared here (the server sends far more than this app needs).
 */
data class WeatherResponse(
    val coord: Coord,
    val weather: List<Weather>,
    val main: Main,
    val name: String,
    val dt: Long,
    val timezone: Int,
)

/** Geographic coordinates of the city. */
data class Coord(
    val lon: Double,
    val lat: Double,
)

/**
 * One weather condition, e.g. `main = "Clouds"` and
 * `description = "broken clouds"`.
 *
 * [icon] is the name of the PNG file (for example `04n`) that is served from
 * `https://openweathermap.org/img/wn/{icon}@2x.png`.
 */
data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String,
)

/** Temperature, pressure and humidity. */
data class Main(
    val temp: Double,
    // The server calls this field "feels_like".
    @Json(name = "feels_like") val feelsLike: Double,
    val pressure: Int,
    val humidity: Int,
)
