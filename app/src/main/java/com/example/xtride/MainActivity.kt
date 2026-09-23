package com.example.xtride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.xtride.data.local.AppDatabase
import com.example.xtride.data.repository.AuthRepository
import com.example.xtride.data.repository.UserProfileRepository
import com.example.xtride.feature.auth.AuthFlow
import com.example.xtride.feature.auth.AuthViewModel
import com.example.xtride.feature.splash.SplashScreen
import com.example.xtride.navigation.MainScaffold
import com.example.xtride.ui.theme.XtrideTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Local Room Database & Repositories
        val database = AppDatabase.getInstance(applicationContext)
        val userProfileRepo = UserProfileRepository(database.userProfileDao())
        val authRepo = AuthRepository()

        setContent {
            XtrideTheme {
                var showSplash by remember { mutableStateOf(true) }
                val authViewModel = remember { AuthViewModel(authRepo, userProfileRepo) }

                // Track authentication status dynamically
                var isLoggedIn by remember {
                    mutableStateOf(authRepo.currentUser != null)
                }

                if (showSplash) {
                    SplashScreen(
                        onSplashFinished = {
                            showSplash = false
                        }
                    )
                } else if (isLoggedIn) {
                    MainScaffold()
                } else {
                    AuthFlow(
                        viewModel = authViewModel,
                        onAuthComplete = {
                            isLoggedIn = true
                        }
                    )
                }
            }
        }
    }
}