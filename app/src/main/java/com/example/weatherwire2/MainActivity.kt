/* Name: Malcolm White
 * Date: 2025-03-23
 * Description: Main activity for the weatherwire app.
 */

package com.example.weatherwire2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.weatherwire2.screens.FavoritesScreen
import com.example.weatherwire2.screens.HomeScreen
import com.example.weatherwire2.screens.SearchScreen
import com.example.weatherwire2.screens.SettingsScreen
import com.example.weatherwire2.ui.theme.WeatherAppTheme
import com.google.firebase.FirebaseApp

// Main activity which will call WeatherApp()
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            WeatherApp()
        }
    }
}

// WeatherApp composable with navigation bar and theme state
@Composable
fun WeatherApp() {
    val navController = rememberNavController()

    val systemDarkTheme = isSystemInDarkTheme()

    var isDarkMode by remember { mutableStateOf(systemDarkTheme) }

    WeatherAppTheme(darkTheme = isDarkMode) {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                NavigationGraph(
                    navController = navController,
                    isDarkMode = isDarkMode,
                    onSetDarkMode = { shouldBeDark -> isDarkMode = shouldBeDark }
                )
            }
        }
    }
}


// Navigation bar composable featuring home, search, favorites, and settings screen.
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Search,
        NavigationItem.Favorites,
        NavigationItem.Settings
    )

    NavigationBar {
        val currentRoute = currentRoute(navController)
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

// NavGraph to house composables for screens
@Composable
fun NavigationGraph(
    navController: NavHostController,
    isDarkMode: Boolean,
    onSetDarkMode: (Boolean) -> Unit
) {
    NavHost(navController, startDestination = NavigationItem.Home.route) {
        composable(NavigationItem.Home.route) { HomeScreen() } // Home
        composable(NavigationItem.Search.route) { SearchScreen() } // Search
        composable(NavigationItem.Favorites.route) { FavoritesScreen() } // Fav's
        composable(NavigationItem.Settings.route) {
            // Pass the theme state and update function to the SettingsScreen
            SettingsScreen(
                isDarkMode = isDarkMode,
                onSetDarkMode = onSetDarkMode
            )
        } // Settings
    }
}

// Nav items
sealed class NavigationItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : NavigationItem("home", "Home", Icons.Default.Home)
    object Search : NavigationItem("search", "Search", Icons.Default.Search)
    object Favorites : NavigationItem("favorites", "Favorites", Icons.Default.Favorite)
    object Settings : NavigationItem("settings", "Settings", Icons.Default.Settings)
}


@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}