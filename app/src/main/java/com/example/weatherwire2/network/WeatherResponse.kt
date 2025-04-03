/* Name: Malcolm White
 * Date: 2025-03-23
 * Description: Data classes that define variables used in the weather app for weather data.
 */

package com.example.weatherwire2.network

// WeatherResponse
data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val name: String
)

// Temperature data
data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)

// Weather description data
data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String // Revisit. Icon can maybe be larger, more detailed
)