/* Name: Malcolm White
 * Date: 2025-03-23
 * Description: Main activity for the weatherwire app.
 */

package com.example.weatherwire2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.weatherwire2.screens.HomeScreen
import com.example.weatherwire2.screens.SearchScreen
import com.example.weatherwire2.screens.FavoritesScreen
import com.example.weatherwire2.screens.SettingsScreen
import com.example.weatherwire2.ui.theme.WeatherAppTheme
import androidx.navigation.NavHostController
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.google.firebase.FirebaseApp

// Main activity which will call WeatherApp()
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            WeatherAppTheme {
                WeatherApp()
            }
        }
    }
}

// WeatherApp composavble with navigation bar.
@Composable
fun WeatherApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavigationGraph(navController)
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

    //Navbar instantiation
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
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = NavigationItem.Home.route) {
        composable(NavigationItem.Home.route) { HomeScreen() } // Home
        composable(NavigationItem.Search.route) { SearchScreen() } // Search
        composable(NavigationItem.Favorites.route) { FavoritesScreen() } // Fav's
        composable(NavigationItem.Settings.route) { SettingsScreen() } // Settings
    }
}

// Nav items
sealed class NavigationItem(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
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
