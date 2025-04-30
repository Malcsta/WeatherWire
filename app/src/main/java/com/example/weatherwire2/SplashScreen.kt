/** Name: Malcolm White
 * Date: 2025-04-02
 * Description: A retrofit instance used for making HTTP requests to the weather API.
 */

package com.example.weatherwire2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Splash screen class
class SplashScreen : ComponentActivity() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calling composable
        setContent {
            SplashScreenContent()
        }

        // Delay the splash screen for a few seconds before transitioning to MainActivity
        GlobalScope.launch {
            delay(5000) // 2-second delay
            startActivity(Intent(this@SplashScreen, MainActivity::class.java))
            finish() // Close the splash activity so the user can't return
        }
    }
}

// Splash screen composable
@Composable
fun SplashScreenContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Housing for image (jpg in drawable folder)
        Image(
            painter = painterResource(id = R.drawable.splash_screen),
            contentDescription = "Splash Screen Image",
            modifier = Modifier.fillMaxSize()
        )
    }
}