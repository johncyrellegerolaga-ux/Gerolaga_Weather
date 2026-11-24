package com.example.newyorkweather.data

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Checks that the model matches the JSON OpenWeatherMap actually sends.
 * The payload below is a real "Current weather data" response for New York,
 * trimmed to the fields this app cares about (Moshi ignores the rest).
 */
class WeatherResponseTest {

    private val adapter = WeatherApiClient.moshi.adapter(WeatherResponse::class.java)

    @Test
    fun `parses a current weather response`() {
        val json = """
            {
              "coord": { "lon": -74.006, "lat": 40.7128 },
              "weather": [
                { "id": 803, "main": "Clouds", "description": "broken clouds", "icon": "04n" }
              ],
              "base": "stations",
              "main": {
                "temp": 55.2, "feels_like": 53.1, "temp_min": 53.0, "temp_max": 58.0,
                "pressure": 1015, "humidity": 62
              },
              "visibility": 10000,
              "wind": { "speed": 3.6, "deg": 250 },
              "clouds": { "all": 75 },
              "dt": 1732123456,
              "sys": { "type": 2, "id": 2000000, "country": "US", "sunrise": 1732100000, "sunset": 1732130000 },
              "timezone": -18000,
              "id": 5128581,
              "name": "New York",
              "cod": 200
            }
        """.trimIndent()

        val response = requireNotNull(adapter.fromJson(json))

        assertEquals("New York", response.name)
        assertEquals(40.7128, response.coord.lat, 0.0001)
        assertEquals(53.1, response.main.feelsLike, 0.0001)
        assertEquals(1, response.weather.size)
        assertEquals("Clouds", response.weather.first().main)
        assertEquals("broken clouds", response.weather.first().description)
        assertEquals("04n", response.weather.first().icon)
    }
}
