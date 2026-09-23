package com.example.xtride.feature.auth

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AuthFlow(
    viewModel: AuthViewModel,
    onAuthComplete: () -> Unit
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = onAuthComplete
            )
        }
        composable("register") {
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegistrationSuccess = { navController.navigate("onboarding") }
            )
        }
        composable("onboarding") {
            OnboardingSetupScreen(
                viewModel = viewModel,
                onComplete = onAuthComplete
            )
        }
    }
}