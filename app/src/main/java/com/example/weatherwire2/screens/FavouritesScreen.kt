/* Name: Malcolm White
 * Date: 2025-03-23
 * Description: Favourites screen code for WeatherWire
 */

package com.example.weatherwire2.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.weatherwire2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

enum class ScreenState {
    Main, Login, SignUp
}

// Favourites screen composable
@Composable
fun FavoritesScreen() {
    val auth = FirebaseAuth.getInstance()

    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var screenState by remember { mutableStateOf(ScreenState.Main) }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.favourites_screen),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        when (screenState) {
            ScreenState.Main -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (currentUser == null) {
                        Text("Please log in, sign up, or continue as guest.")
                        Spacer(modifier = Modifier.height(16.dp))
                        Row {
                            Button(onClick = { screenState = ScreenState.Login }) {
                                Text("Login")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { screenState = ScreenState.SignUp }) {
                                Text("Sign Up")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                // Sign in as a guest (anonymous authentication)
                                auth.signInAnonymously()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            currentUser = auth.currentUser
                                        } else {
                                            Log.e("Guest Login", "Failed to sign in as guest: ${task.exception?.message}")
                                        }
                                    }
                            }) {
                                Text("Continue as Guest")
                            }
                        }
                    } else {
                        FavoriteLocationsUI(currentUser!!)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            auth.signOut()
                            currentUser = null
                        }) {
                            Text("Log Out")
                        }
                    }
                }
            }
            ScreenState.Login -> {
                LoginScreen(
                    onLoginSuccess = {
                        currentUser = auth.currentUser
                        screenState = ScreenState.Main
                    },
                    onBack = { screenState = ScreenState.Main }
                )
            }
            ScreenState.SignUp -> {
                SignUpScreen(
                    onSignUpSuccess = {
                        currentUser = auth.currentUser
                        screenState = ScreenState.Main
                    },
                    onBack = { screenState = ScreenState.Main }
                )
            }
        }
    }
}

// Function to save favourites
fun saveFavorites(userId: String, favorites: List<String>) {
    val db = FirebaseFirestore.getInstance()
    val userDocRef = db.collection("users").document(userId)
    val data = hashMapOf(
        "favorites" to favorites
    )

    userDocRef.set(data, SetOptions.merge())
        .addOnSuccessListener {
            Log.d("Favorites", "Favorites saved successfully for user $userId")
        }
        .addOnFailureListener { e ->
            // Log the error in detail if saving fails
            Log.e("Favorites", "Failed to save favorites for user $userId: ${e.message}", e)
        }
}

// Function to load favourites
fun loadFavorites(userId: String, onComplete: (List<String>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(userId)
        .get()
        .addOnSuccessListener { document ->
            val favorites = document.get("favorites") as? List<String> ?: emptyList()
            onComplete(favorites)
        }
        .addOnFailureListener { e ->
            Log.e("Favorites", "Failed to load favorites: ${e.message}")
            onComplete(emptyList())
        }
}

// Login screen composable
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Login")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onLoginSuccess()
                    } else {
                        errorMessage = task.exception?.message
                    }
                }
        }) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { onBack() }) {
            Text("Back")
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
    }
}

// Signup screen composable
@Composable
fun SignUpScreen(onSignUpSuccess: () -> Unit, onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance() // Added Firestore instance

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") } // NEW
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Sign Up")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField( // NEW Confirm Password field
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (password != confirmPassword) {
                errorMessage = "Passwords do not match"
                return@Button
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // After signup success, save user info into Firestore
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            val user = hashMapOf(
                                "email" to email,
                                "createdAt" to FieldValue.serverTimestamp()
                            )
                            db.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener {
                                    onSignUpSuccess()
                                }
                                .addOnFailureListener { e ->
                                    errorMessage = "Error saving user: ${e.message}"
                                }
                        } else {
                            errorMessage = "User ID is null after sign-up"
                        }
                    } else {
                        errorMessage = task.exception?.message
                    }
                }
        }) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { onBack() }) {
            Text("Back")
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
    }
}

// Favourite locations composable
@Composable
fun FavoriteLocationsUI(currentUser: FirebaseUser) {
    val db = FirebaseFirestore.getInstance()
    var citySearch by remember { mutableStateOf("") }
    var favorites by remember { mutableStateOf(listOf<String>()) }
    var searchResults by remember { mutableStateOf(listOf<String>()) }
    val maxFavorites = 5

    val allCities = listOf(
        "New York", "Los Angeles", "Chicago", "Houston", "Toronto",
        "Paris", "London", "Tokyo", "Seoul", "Sydney", "Melbourne", "Berlin", "Madrid"
    )

    // Load favorites from Firestore once
    LaunchedEffect(currentUser.uid) {
        loadFavorites(currentUser.uid) { loadedFavorites ->
            favorites = loadedFavorites
        }
    }

    LaunchedEffect(citySearch) {
        searchResults = if (citySearch.isNotBlank()) {
            allCities.filter { it.contains(citySearch, ignoreCase = true) }
        } else {
            emptyList()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Select up to $maxFavorites Favorite Cities:")
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = citySearch,
                onValueChange = { citySearch = it },
                label = { Text("Search for a city") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val matchingCity = allCities.firstOrNull { it.equals(citySearch.trim(), ignoreCase = true) }
                    if (matchingCity != null && favorites.size < maxFavorites && !favorites.contains(matchingCity)) {
                        favorites = favorites + matchingCity
                        citySearch = "" // Clear after adding
                    }
                },
                enabled = citySearch.isNotBlank() && favorites.size < maxFavorites,
            ) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Show search results
        searchResults.forEach { city ->
            Button(
                onClick = {
                    if (favorites.size < maxFavorites && !favorites.contains(city)) {
                        favorites = favorites + city
                    }
                },
                enabled = favorites.size < maxFavorites && !favorites.contains(city),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Text(city)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Selected Favorites:")
        favorites.forEach { city ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(city)
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        favorites = favorites - city
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.onError)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                saveFavorites(currentUser.uid, favorites)
            },
            enabled = favorites.isNotEmpty()
        ) {
            Text("Save Favorites")
        }
    }
}
