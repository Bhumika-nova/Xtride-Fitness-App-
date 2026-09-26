package com.example.xtride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.xtride.data.local.AppDatabase
import com.example.xtride.data.repository.AuthRepository
import com.example.xtride.data.repository.RunRepository
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
    private var targetTab by mutableStateOf<String?>(null)
    private var showFinishPrompt by mutableStateOf(false)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: android.content.Intent?) {
        if (intent?.getStringExtra("OPEN_TAB") == "TRACK") {
            targetTab = "TRACK"
        }
        if (intent?.getBooleanExtra("SHOW_FINISH_DIALOG", false) == true) {
            showFinishPrompt = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleNotificationIntent(intent)
        enableEdgeToEdge()

        // Request Activity Recognition Permission (Required for step counter on Android 10+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), 1001)
            }
        }

        // Initialize OSMDroid with compliant custom User-Agent to prevent HTTP 403
        org.osmdroid.config.Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid_prefs", MODE_PRIVATE)
        )
        org.osmdroid.config.Configuration.getInstance().userAgentValue = "XtrideRunnerApp/1.0 (fitness-tracking; support@xtride.org)"

        // Initialize Local Room Database, Repositories & Sensor Manager
        val database = AppDatabase.getInstance(applicationContext)
        val userProfileRepo = UserProfileRepository(database.userProfileDao())
        val stepRepo = StepRepository(database.dailyStepsDao(), userProfileRepo)
        val workoutRepo = WorkoutRepository(database.workoutDao(), userProfileRepo)
        val runRepo = RunRepository(database.runSessionDao(), userProfileRepo)
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
                        runRepo = runRepo,
                        sensorManager = sensorManager,
                        authRepo = authRepo,
                        targetTab = targetTab,
                        showFinishPrompt = showFinishPrompt,
                        onFinishPromptHandled = {
                            showFinishPrompt = false
                            targetTab = null
                        },
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