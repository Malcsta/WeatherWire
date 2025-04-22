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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

enum class ScreenState {
    Main, Login, SignUp
}

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
                        Text("Please log in or sign up.")
                        Spacer(modifier = Modifier.height(16.dp))
                        Row {
                            Button(onClick = { screenState = ScreenState.Login }) {
                                Text("Login")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { screenState = ScreenState.SignUp }) {
                                Text("Sign Up")
                            }
                        }
                    } else {
                        Text("Welcome, ${currentUser?.email ?: "User"}!")
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


fun saveFavorites(userId: String, favorites: List<String>) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(userId)
        .update("favorites", favorites)
        .addOnSuccessListener {
            Log.d("Favorites", "Favorites saved successfully")
        }
        .addOnFailureListener { e ->
            Log.e("Favorites", "Failed to save favorites: ${e.message}")
        }
}

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

@Composable
fun FavoriteLocationsUI() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Your Favorite Weather Spots:")
        Spacer(modifier = Modifier.height(8.dp))
        // Example spots - you can make this dynamic later!
        Button(onClick = { /* TODO: Handle favorite selection */ }) {
            Text("New York")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { /* TODO: Handle favorite selection */ }) {
            Text("Tokyo")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { /* TODO: Handle favorite selection */ }) {
            Text("Paris")
        }
    }
}