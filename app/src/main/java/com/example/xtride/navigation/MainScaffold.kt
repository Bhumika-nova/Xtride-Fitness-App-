package com.example.xtride.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.xtride.data.repository.AuthRepository
import com.example.xtride.data.repository.StepRepository
import com.example.xtride.data.repository.UserProfileRepository
import com.example.xtride.data.repository.WorkoutRepository
import com.example.xtride.data.sensor.StepSensorManager
import com.example.xtride.feature.analytics.AnalyticsScreen
import com.example.xtride.feature.analytics.AnalyticsViewModel
import com.example.xtride.feature.home.HomeScreen
import com.example.xtride.feature.home.HomeViewModel
import com.example.xtride.feature.profile.ProfileScreen
import com.example.xtride.feature.profile.ProfileViewModel
import com.example.xtride.feature.workout.WorkoutScreen
import com.example.xtride.feature.workout.WorkoutViewModel

@Composable
fun MainScaffold(
    stepRepo: StepRepository,
    userProfileRepo: UserProfileRepository,
    workoutRepo: WorkoutRepository,
    sensorManager: StepSensorManager?,
    authRepo: AuthRepository? = null,
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val homeViewModel = remember {
        HomeViewModel(
            stepRepo = stepRepo,
            userProfileRepo = userProfileRepo,
            sensorManager = sensorManager
        )
    }

    val workoutViewModel = remember {
        WorkoutViewModel(workoutRepo = workoutRepo)
    }

    val analyticsViewModel = remember {
        AnalyticsViewModel(
            stepRepo = stepRepo,
            workoutRepo = workoutRepo
        )
    }

    val profileViewModel = remember {
        ProfileViewModel(
            userProfileRepo = userProfileRepo,
            authRepo = authRepo
        )
    }

    val screens = listOf(
        Screen.Home,
        Screen.Workout,
        Screen.Analytics,
        Screen.Profile
    )

    Scaffold(
        containerColor = Color(0xFF040711),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0A0F1D),
                tonalElevation = 8.dp
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(text = screen.title)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE11D48),
                            selectedTextColor = Color(0xFFE11D48),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = Color(0xFFE11D48).copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Workout.route) {
                WorkoutScreen(viewModel = workoutViewModel)
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(viewModel = analyticsViewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}