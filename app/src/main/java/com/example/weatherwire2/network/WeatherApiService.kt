/* Name: Malcolm White
 * Date: 2025-03-23
 * Description: Interface for the weather API service. Can GET weather data either by city or
 * by latitude and longitude coordinates.
 */

package com.example.weatherwire2.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// Weather by city
interface WeatherApiService {
    @GET("weather")
    fun getWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): Call<WeatherResponse>

    // Weather by coordinates (lat, long)
    @GET("weather")
    fun getWeatherByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): Call<WeatherResponse>
}