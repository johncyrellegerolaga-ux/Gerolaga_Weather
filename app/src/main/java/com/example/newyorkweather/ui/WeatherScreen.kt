package com.example.newyorkweather.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.Glide
import com.example.newyorkweather.R
import com.example.newyorkweather.data.Coord
import com.example.newyorkweather.data.Main
import com.example.newyorkweather.data.Weather
import com.example.newyorkweather.data.WeatherResponse
import com.example.newyorkweather.ui.theme.NewYorkWeatherTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

/** Base URL of the PNG icons, using @4x resolution for high crispness. */
private const val ICON_BASE_URL = "https://openweathermap.org/img/wn/"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    uiState: WeatherUiState,
    onRetry: () -> Unit,
    onCitySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSearchDialog by remember { mutableStateOf(false) }
    var cityInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search City",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (uiState) {
                WeatherUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )

                is WeatherUiState.Success -> WeatherDetails(
                    weather = uiState.weather,
                    fetchedAt = uiState.fetchedAt,
                    modifier = Modifier.fillMaxSize(),
                )

                is WeatherUiState.Error -> ErrorMessage(
                    reason = uiState.reason,
                    code = uiState.code,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }

    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = { Text("Search City") },
            text = {
                OutlinedTextField(
                    value = cityInput,
                    onValueChange = { cityInput = it },
                    label = { Text("City name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (cityInput.isNotBlank()) {
                            onCitySelected(cityInput)
                            cityInput = ""
                            showSearchDialog = false
                        }
                    }
                ) {
                    Text("Search")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSearchDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

/** City name, short description, long description and the condition icon. */
@Composable
private fun WeatherDetails(
    weather: WeatherResponse,
    fetchedAt: Long,
    modifier: Modifier = Modifier,
) {
    val condition = weather.weather.firstOrNull()

    // Localized clock time of the response, e.g. "Updated 4:55 PM".
    val context = LocalContext.current
    val updatedAt = remember(fetchedAt) {
        android.text.format.DateFormat.getTimeFormat(context).format(Date(fetchedAt))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = weather.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = condition?.main.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = condition?.description.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.updated_at, updatedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (condition != null) {
            WeatherIcon(
                iconCode = condition.icon,
                mainCondition = condition.main,
                modifier = Modifier.size(72.dp),
            )
        }
    }
}

/**
 * Downloads the PNG for [iconCode] with Glide and shows it once it is decoded.
 * Falls back to a clear vector icon (e.g. bright yellow sun for clear day,
 * crescent moon for clear night) so clear days are distinctly represented.
 */
@Composable
fun WeatherIcon(
    iconCode: String,
    modifier: Modifier = Modifier,
    mainCondition: String = "",
) {
    val context = LocalContext.current
    val iconUrl = "$ICON_BASE_URL$iconCode@4x.png"

    val bitmap by produceState<ImageBitmap?>(initialValue = null, iconUrl) {
        val request = Glide.with(context).asBitmap().load(iconUrl).submit()
        value = withContext(Dispatchers.IO) {
            runCatching { request.get().asImageBitmap() }.getOrNull()
        }
    }

    val (vectorIcon: ImageVector, vectorColor: Color) = remember(iconCode, mainCondition) {
        getWeatherVectorIcon(iconCode, mainCondition)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = stringResource(R.string.weather_icon),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        } else {
            Icon(
                imageVector = vectorIcon,
                contentDescription = stringResource(R.string.weather_icon),
                tint = vectorColor,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private fun getWeatherVectorIcon(iconCode: String, mainCondition: String): Pair<ImageVector, Color> {
    val isNight = iconCode.endsWith("n")
    return when {
        // Clear Day -> Bright Sun
        (iconCode == "01d") || (mainCondition.equals("Clear", ignoreCase = true) && !isNight) -> {
            Icons.Filled.WbSunny to Color(0xFFFFB300)
        }
        // Clear Night -> Crescent Moon
        (iconCode == "01n") || (mainCondition.equals("Clear", ignoreCase = true) && isNight) -> {
            Icons.Filled.NightsStay to Color(0xFF7E57C2)
        }
        // Few / Scattered Clouds
        iconCode in listOf("02d", "02n", "03d", "03n") -> {
            Icons.Filled.WbCloudy to Color(0xFF42A5F5)
        }
        // Broken / Overcast Clouds
        (iconCode in listOf("04d", "04n")) || mainCondition.equals("Clouds", ignoreCase = true) -> {
            Icons.Filled.Cloud to Color(0xFF78909C)
        }
        // Rain / Drizzle
        (iconCode in listOf("09d", "09n", "10d", "10n")) ||
                mainCondition.equals("Rain", ignoreCase = true) ||
                mainCondition.equals("Drizzle", ignoreCase = true) -> {
            Icons.Filled.WaterDrop to Color(0xFF29B6F6)
        }
        // Thunderstorm
        (iconCode in listOf("11d", "11n")) || mainCondition.equals("Thunderstorm", ignoreCase = true) -> {
            Icons.Filled.Thunderstorm to Color(0xFFFFA000)
        }
        // Snow
        (iconCode in listOf("13d", "13n")) || mainCondition.equals("Snow", ignoreCase = true) -> {
            Icons.Filled.AcUnit to Color(0xFF4DD0E1)
        }
        // Mist / Fog / Haze
        (iconCode in listOf("50d", "50n")) ||
                mainCondition.contains("Mist", ignoreCase = true) ||
                mainCondition.contains("Fog", ignoreCase = true) ||
                mainCondition.contains("Haze", ignoreCase = true) -> {
            Icons.Filled.Dehaze to Color(0xFF90A4AE)
        }
        // Default Clear Day / Night fallback
        else -> {
            if (isNight) {
                Icons.Filled.NightsStay to Color(0xFF7E57C2)
            } else {
                Icons.Filled.WbSunny to Color(0xFFFFB300)
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    reason: WeatherError,
    code: Int?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message = when (reason) {
        WeatherError.MISSING_API_KEY -> stringResource(R.string.error_missing_api_key)
        WeatherError.INVALID_API_KEY -> stringResource(R.string.error_invalid_api_key)
        WeatherError.CITY_NOT_FOUND -> stringResource(R.string.error_city_not_found)
        WeatherError.NETWORK -> stringResource(R.string.error_network)
        WeatherError.SERVER -> stringResource(R.string.error_server, code ?: 0)
        WeatherError.UNEXPECTED -> stringResource(R.string.error_unexpected)
    }

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

private val previewClearWeather = WeatherResponse(
    coord = Coord(lon = -74.006, lat = 40.7128),
    weather = listOf(Weather(id = 800, main = "Clear", description = "clear sky", icon = "01d")),
    main = Main(temp = 72.0, feelsLike = 70.0, pressure = 1015, humidity = 45),
    name = "New York",
    dt = 1732123456L,
    timezone = -18000,
)

private val previewCloudyWeather = WeatherResponse(
    coord = Coord(lon = -74.006, lat = 40.7128),
    weather = listOf(Weather(id = 803, main = "Clouds", description = "broken clouds", icon = "04n")),
    main = Main(temp = 55.0, feelsLike = 53.0, pressure = 1015, humidity = 62),
    name = "New York",
    dt = 1732123456L,
    timezone = -18000,
)

@Preview(showBackground = true)
@Composable
private fun WeatherScreenClearSkyPreview() {
    NewYorkWeatherTheme(dynamicColor = false) {
        WeatherScreen(uiState = WeatherUiState.Success(previewClearWeather, fetchedAt = 1732123456000L), onRetry = {}, onCitySelected = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherScreenCloudyPreview() {
    NewYorkWeatherTheme(dynamicColor = false) {
        WeatherScreen(uiState = WeatherUiState.Success(previewCloudyWeather, fetchedAt = 1732123456000L), onRetry = {}, onCitySelected = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherScreenErrorPreview() {
    NewYorkWeatherTheme(dynamicColor = false) {
        WeatherScreen(uiState = WeatherError.NETWORK.let { WeatherUiState.Error(it) }, onRetry = {}, onCitySelected = {})
    }
}
