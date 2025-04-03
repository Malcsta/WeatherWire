/** Name: Malcolm White
 * Date: 2025-03-23
 * Description: A retrofit instance used for making HTTP requests to the weather API.
 */
package com.example.weatherwire2.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// API endpoint
private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

// Getting retrofit instance with API
fun getRetrofitInstance(): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
