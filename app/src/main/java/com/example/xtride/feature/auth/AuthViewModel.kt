package com.example.xtride.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xtride.data.local.entity.UserProfileEntity
import com.example.xtride.data.repository.AuthRepository
import com.example.xtride.data.repository.UserProfileRepository
import com.example.xtride.domain.model.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Authenticated(val user: AuthUser) : AuthUiState
    data class Error(val message: String) : AuthUiState
    object OnboardingComplete : AuthUiState
}

class AuthViewModel(
    private val authRepo: AuthRepository,
    private val userProfileRepo: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(
        if (authRepo.currentUser != null) AuthUiState.Authenticated(authRepo.currentUser!!)
        else AuthUiState.Idle
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in all fields")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepo.loginWithEmail(email, pass)
            result.onSuccess { user ->
                syncUserProfile(user)
                _uiState.value = AuthUiState.Authenticated(user)
            }.onFailure { error ->
                val msg = error.localizedMessage.orEmpty()
                val userFriendlyMessage = when {
                    error is com.google.firebase.auth.FirebaseAuthInvalidUserException ||
                    msg.contains("no user record", ignoreCase = true) ||
                    msg.contains("user-not-found", ignoreCase = true) -> {
                        "No account found with this email. Please sign up before logging in."
                    }
                    error is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ||
                    msg.contains("invalid-credential", ignoreCase = true) ||
                    msg.contains("wrong-password", ignoreCase = true) ||
                    msg.contains("password", ignoreCase = true) -> {
                        "Incorrect email or password. If you don't have an account, please sign up first."
                    }
                    error is com.google.firebase.FirebaseNetworkException ||
                    msg.contains("network", ignoreCase = true) -> {
                        "Network error. Please check your internet connection and try again."
                    }
                    else -> {
                        error.localizedMessage ?: "Authentication failed. Please sign up or try again."
                    }
                }
                _uiState.value = AuthUiState.Error(userFriendlyMessage)
            }
        }
    }

    fun signUp(name: String, email: String, pass: String, confirmPass: String, gender: String = "Female") {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in all fields")
            return
        }
        if (pass != confirmPass) {
            _uiState.value = AuthUiState.Error("Passwords do not match")
            return
        }
        if (pass.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepo.signUpWithEmail(name, email, pass)
            result.onSuccess { user ->
                syncUserProfile(user, gender)
                _uiState.value = AuthUiState.Authenticated(user)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Sign up failed")
            }
        }
    }

    fun saveOnboardingProfile(
        heightCm: Float,
        weightKg: Float,
        age: Int,
        gender: String,
        stepGoal: Int
    ) {
        val user = authRepo.currentUser
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val entity = UserProfileEntity(
                firebaseUid = user?.uid ?: "offline_athlete",
                fullName = user?.displayName ?: "Athlete",
                email = user?.email ?: "athlete@xtride.local",
                photoUrl = user?.photoUrl,
                heightCm = heightCm,
                weightKg = weightKg,
                age = age,
                gender = gender,
                dailyStepGoal = stepGoal,
                isDarkMode = false
            )
            userProfileRepo.saveProfile(entity)
            _uiState.value = AuthUiState.OnboardingComplete
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    // Google Sign-In
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepo.loginWithGoogle(idToken)
            result.onSuccess { user ->
                syncUserProfile(user)
                _uiState.value = AuthUiState.Authenticated(user)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Google sign in failed")
            }
        }
    }

    private suspend fun syncUserProfile(user: AuthUser, gender: String? = null) {
        val existing = userProfileRepo.getProfile()
        val displayName = user.displayName.ifBlank { user.email.substringBefore("@").ifBlank { "Athlete" } }
        val updated = if (existing == null) {
            UserProfileEntity(
                firebaseUid = user.uid,
                fullName = displayName,
                email = user.email,
                photoUrl = user.photoUrl,
                heightCm = 170f,
                weightKg = 68f,
                age = 25,
                gender = gender ?: "Not specified",
                dailyStepGoal = 6000
            )
        } else {
            existing.copy(
                firebaseUid = user.uid,
                fullName = displayName,
                email = user.email.ifBlank { existing.email },
                photoUrl = user.photoUrl ?: existing.photoUrl,
                gender = gender ?: existing.gender,
                updatedAt = System.currentTimeMillis()
            )
        }
        userProfileRepo.saveProfile(updated)
    }
}