/* Name: Malcolm White
 * Date: 2025-03-23
 * Description: Search screen composable, where the user can perform a search of a specified city.
 */

package com.example.weatherwire2.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.weatherwire2.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherwire2.network.WeatherApiService
import com.example.weatherwire2.network.WeatherResponse
import com.example.weatherwire2.network.getRetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/// SearchScreen Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    var locationInput by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Function to fetch weather data based on location name
    fun fetchWeatherByLocation(location: String) {
        if (location.isBlank()) {
            errorMessage = "Please enter a location"
            return
        }

        // Defaults
        isLoading = true
        errorMessage = ""
        temperature = ""
        description = ""

        // Calling retrofitinstance
        val weatherApiService = getRetrofitInstance().create(WeatherApiService::class.java)
        val call = weatherApiService.getWeatherByCity(
            location,
            "eb4cbae2b1a8d8e2fd251eb9d428c3f0",
            "metric"
        )

        // Weather API response
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                isLoading = false

                // Check if successful, and if so assign our values for data
                if (response.isSuccessful) {
                    val weatherData = response.body()
                    weatherData?.let {
                        temperature = "${it.main.temp}°C"
                        description = it.weather.firstOrNull()?.description ?: "N/A"
                        errorMessage = ""
                    }
                    // not successful, so display error
                } else {
                    when (response.code()) {
                        404 -> errorMessage = "Location not found"
                        401 -> errorMessage = "Invalid API key"
                        else -> errorMessage = "Error: ${response.code()}"
                    }
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                isLoading = false
                errorMessage = "Network error: ${t.message}"
            }
        })
    }

    // Layour
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.home_screen),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // weather results
            if (temperature.isNotEmpty() && description.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = temperature,
                        fontSize = 64.sp,
                        color = Color.White
                    )
                    Text(
                        text = locationInput,
                        fontSize = 36.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description.replaceFirstChar { it.uppercase() },
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
            // Location input field
            OutlinedTextField(
                value = locationInput,
                onValueChange = { locationInput = it },
                label = { Text("Enter Location") },
                placeholder = { Text("e.g., London, New York, Tokyo") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Search button
            Button(
                onClick = {
                    fetchWeatherByLocation(locationInput)
                    focusManager.clearFocus()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Get Weather", fontSize = 16.sp)
            }

            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Error message
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
