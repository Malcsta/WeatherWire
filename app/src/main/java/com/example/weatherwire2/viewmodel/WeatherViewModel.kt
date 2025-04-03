package com.example.weatherwire2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherwire2.network.WeatherApiService
import com.example.weatherwire2.network.WeatherResponse
import com.example.weatherwire2.network.getRetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Weather view model
class WeatherViewModel : ViewModel() {


    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData: LiveData<WeatherResponse> = _weatherData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val apiService = getRetrofitInstance().create(WeatherApiService::class.java)

    fun fetchWeatherByCity(cityName: String) {
        if (cityName.isBlank()) {
            _errorMessage.value = "Please enter a location"
            return
        }

        _isLoading.value = true
        _errorMessage.value = ""

        // API service with API key, specified metrics (change later)
        apiService.getWeatherByCity(cityName, "eb4cbae2b1a8d8e2fd251eb9d428c3f0", "metric")
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        _weatherData.value = response.body()
                    } else {
                        when (response.code()) {
                            404 -> _errorMessage.value = "Location not found"
                            401 -> _errorMessage.value = "Invalid API key"
                            else -> _errorMessage.value = "Error: ${response.code()}"
                        }
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = "Network error: ${t.message}"
                }
            })
    }
}