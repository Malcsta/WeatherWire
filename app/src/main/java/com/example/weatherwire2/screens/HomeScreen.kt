package com.example.weatherwire2.screens

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.weatherwire2.network.WeatherApiService
import com.example.weatherwire2.network.WeatherResponse
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.weatherwire2.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import coil3.compose.rememberAsyncImagePainter
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.delay
import retrofit2.http.GET
import retrofit2.http.Query

// Home Screen composable
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen() {
    var weatherResponse by remember { mutableStateOf<WeatherResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var newsArticle by remember { mutableStateOf<NewsArticle?>(null) }

    val context = LocalContext.current
    val permissionState = rememberPermissionState(permission = android.Manifest.permission.ACCESS_FINE_LOCATION)

    // fetch location and weather when the screen is launched
    LaunchedEffect(true) {
        if (permissionState.status.isGranted) {
            getCurrentLocation(context = context) { location ->
                if (location != null) {
                    fetchWeatherByCoordinates(
                        // coords
                        lat = location.latitude,
                        lon = location.longitude,
                        onSuccess = {
                            weatherResponse = it
                            isLoading = false
                        },
                        onError = {
                            errorMessage = it
                            isLoading = false
                        }
                    )
                } else {
                    errorMessage = "Unable to get location"
                    isLoading = false
                }
            }
        } else {
            permissionState.launchPermissionRequest()
        }
    }

    // Fetch news every 10 seconds (revisit)
    LaunchedEffect(Unit) {
        while (true) {
            fetchNews { newsArticle = it }
            delay(10_000)
        }
    }

    // Box for BG
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.home_screen),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center the weather content
        ) {
            // weather information display logic
            Spacer(modifier = Modifier.weight(1f))
            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else if (errorMessage != null) {
                Text(text = errorMessage ?: "", color = Color.Red)
            } else if (weatherResponse != null) {
                weatherResponse?.let { weather ->
                    val iconUrl = "https://openweathermap.org/img/wn/${weather.weather[0].icon}@2x.png"
                    val painter = rememberAsyncImagePainter(iconUrl)

                    // Icon
                    Image(
                        painter = painter,
                        contentDescription = "Weather Icon",
                        modifier = Modifier.size(100.dp)
                    )
                    // Main temp (add variable in here later for metric/Imperial change)
                    Text(
                        text = "${weather.main.temp}°C",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    // Description
                    Text(
                        text = weather.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Can change later for layout
            Spacer(modifier = Modifier.weight(1f))

            // News Card with full title and image only
            newsArticle?.let { news ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 16.dp),
                    elevation = CardDefaults.elevatedCardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Load and display the news image (if available)
                        news.imageUrl?.let { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "News Image",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = news.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// Function to get current location
fun getCurrentLocation(context: Context, onLocationReceived: (Location?) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationTask: Task<Location> = fusedLocationClient.lastLocation

    locationTask.addOnSuccessListener { location: Location? ->
        if (location != null) {
            Log.d("WeatherApp", "Location fetched: Latitude = ${location.latitude}, Longitude = ${location.longitude}")
        } else {
            Log.d("WeatherApp", "Location is null")
        }
        onLocationReceived(location)
    }
}

// Fetch weather data by coordinates
fun fetchWeatherByCoordinates(
    lat: Double,
    lon: Double,
    onSuccess: (WeatherResponse) -> Unit,
    onError: (String) -> Unit
) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val weatherApiService = retrofit.create(WeatherApiService::class.java)
    val apiKey = "eb4cbae2b1a8d8e2fd251eb9d428c3f0"
    val units = "metric"

    weatherApiService.getWeatherByCoordinates(lat, lon, apiKey, units)
        .enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: onError("No weather data received")
                } else {
                    onError("Failed to fetch weather")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                onError(t.message ?: "Unknown error")
            }
        })
}

// News Data Class with Image URL
data class NewsArticle(val title: String, val description: String?, val imageUrl: String?)

// Fetch news function
fun fetchNews(onNewsReceived: (NewsArticle?) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://newsapi.org/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val newsApi = retrofit.create(NewsApiService::class.java)
    val apiKey = "50a90145aecb4dc09dc64c04d97bbec8"

    newsApi.getTopHeadlines("us", apiKey).enqueue(object : Callback<NewsResponse> {
        override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
            if (response.isSuccessful) {
                val article = response.body()?.articles?.firstOrNull()
                article?.let {
                    onNewsReceived(NewsArticle(it.title, it.description, it.imageUrl))
                }
            } else {
                onNewsReceived(null)
            }
        }

        override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
            onNewsReceived(null)
        }
    })
}

// Retrofit API Interface for News
interface NewsApiService {
    @GET("top-headlines")
    fun getTopHeadlines(
        @Query("country") country: String,
        @Query("apiKey") apiKey: String
    ): Call<NewsResponse>
}

// Data class for news API response
data class NewsResponse(val articles: List<NewsArticleItem>)
data class NewsArticleItem(
    val title: String,
    val description: String?,
    @SerializedName("urlToImage") val imageUrl: String?
)