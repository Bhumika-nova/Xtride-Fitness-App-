package com.example.xtride.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen(
        route = "home",
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    object Track : Screen(
        route = "track",
        title = "Track",
        selectedIcon = Icons.Filled.Timeline,
        unselectedIcon = Icons.Outlined.Timeline
    )
    object Workout : Screen(
        route = "workout",
        title = "Workout",
        selectedIcon = Icons.Filled.FitnessCenter,
        unselectedIcon = Icons.Outlined.FitnessCenter
    )
    object Analytics : Screen(
        route = "analytics",
        title = "Analytics",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    )
    object Profile : Screen(
        route = "profile",
        title = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}