package com.example.newyorkweather

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newyorkweather.ui.WeatherScreen
import com.example.newyorkweather.ui.WeatherViewModel
import com.example.newyorkweather.ui.theme.NewYorkWeatherTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app bar is dark green, so the status bar needs light icons.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            NewYorkWeatherTheme(dynamicColor = false) {
                val viewModel: WeatherViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // Poll only while the screen is in the foreground.
                LifecycleStartEffect(viewModel) {
                    viewModel.startPolling()
                    onStopOrDispose { viewModel.stopPolling() }
                }

                WeatherScreen(
                    uiState = uiState,
                    onRetry = viewModel::refreshNow,
                    onCitySelected = viewModel::setCity,
                )
            }
        }
    }
}
