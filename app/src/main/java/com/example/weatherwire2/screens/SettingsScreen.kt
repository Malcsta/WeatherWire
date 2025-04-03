package com.example.weatherwire2.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.weatherwire2.R

@Composable
fun SettingsScreen() {
    // State variables for the settings
    var selectedUnit by remember { mutableStateOf("°C") }
    var selectedWindSpeed by remember { mutableStateOf("km/h") }
    var isDarkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Image that covers the entire screen
        Image(
            painter = painterResource(id = R.drawable.settings_screen),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Column for the settings, moved down and centered
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center) // Center the content in the Box
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Temperature unit selection (°C or °F)
            Text("Select Temperature Unit")
            Row {
                RadioButton(
                    selected = selectedUnit == "°C",
                    onClick = { selectedUnit = "°C" }
                )
                Text("°C")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = selectedUnit == "°F",
                    onClick = { selectedUnit = "°F" }
                )
                Text("°F")
            }

            // Wind speed unit selection (km/h or mph)
            Text("Select Wind Speed Unit")
            Row {
                RadioButton(
                    selected = selectedWindSpeed == "km/h",
                    onClick = { selectedWindSpeed = "km/h" }
                )
                Text("km/h")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = selectedWindSpeed == "mph",
                    onClick = { selectedWindSpeed = "mph" }
                )
                Text("mph")
            }

            // Theme selection (Light/Dark mode)
            Text("Select Theme")
            Row {
                RadioButton(
                    selected = !isDarkMode,
                    onClick = { isDarkMode = false }
                )
                Text("Light Mode")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = isDarkMode,
                    onClick = { isDarkMode = true }
                )
                Text("Dark Mode")
            }

            // Notification preference toggle
            Text("Enable Notifications")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                Text("Notifications", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
