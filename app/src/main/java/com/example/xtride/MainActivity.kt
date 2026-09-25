package com.example.xtride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.xtride.data.local.AppDatabase
import com.example.xtride.data.repository.AuthRepository
import com.example.xtride.data.repository.StepRepository
import com.example.xtride.data.repository.UserProfileRepository
import com.example.xtride.data.repository.WorkoutRepository
import com.example.xtride.data.sensor.StepSensorManager
import com.example.xtride.feature.auth.AuthFlow
import com.example.xtride.feature.auth.AuthViewModel
import com.example.xtride.feature.splash.SplashScreen
import com.example.xtride.navigation.MainScaffold
import com.example.xtride.ui.theme.XtrideTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request Activity Recognition Permission (Required for step counter on Android 10+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), 1001)
            }
        }

        // Initialize Local Room Database, Repositories & Sensor Manager
        val database = AppDatabase.getInstance(applicationContext)
        val userProfileRepo = UserProfileRepository(database.userProfileDao())
        val stepRepo = StepRepository(database.dailyStepsDao(), userProfileRepo)
        val workoutRepo = WorkoutRepository(database.workoutDao(), userProfileRepo)
        val authRepo = AuthRepository()
        val sensorManager = StepSensorManager(applicationContext)

        setContent {
            XtrideTheme {
                var showSplash by remember { mutableStateOf(true) }
                val authViewModel = remember { AuthViewModel(authRepo, userProfileRepo) }

                var isLoggedIn by remember {
                    mutableStateOf(authRepo.currentUser != null)
                }

                // Strictly enforce authenticated user session (no offline bypass)
                LaunchedEffect(Unit) {
                    userProfileRepo.activeUserProfile.collect { profile ->
                        isLoggedIn = authRepo.currentUser != null && profile != null && profile.firebaseUid != "offline_athlete"
                    }
                }

                if (showSplash) {
                    SplashScreen(
                        onSplashFinished = {
                            showSplash = false
                        }
                    )
                } else if (isLoggedIn) {
                    MainScaffold(
                        stepRepo = stepRepo,
                        userProfileRepo = userProfileRepo,
                        workoutRepo = workoutRepo,
                        sensorManager = sensorManager,
                        authRepo = authRepo,
                        onLogout = {
                            isLoggedIn = false
                        }
                    )
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