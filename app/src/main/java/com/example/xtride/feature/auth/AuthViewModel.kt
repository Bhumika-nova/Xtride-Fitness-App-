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
                val existing = userProfileRepo.getProfile()
                if (existing == null) {
                    userProfileRepo.saveProfile(
                        UserProfileEntity(
                            firebaseUid = user.uid,
                            fullName = user.displayName.ifBlank { "Athlete" },
                            email = user.email,
                            photoUrl = user.photoUrl,
                            heightCm = 170f,
                            weightKg = 68f,
                            age = 25,
                            gender = "Not specified",
                            dailyStepGoal = 6000
                        )
                    )
                }
                _uiState.value = AuthUiState.Authenticated(user)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Login failed")
            }
        }
    }

    fun signUp(name: String, email: String, pass: String, confirmPass: String) {
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
                _uiState.value = AuthUiState.Authenticated(user)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Sign up failed")
            }
        }
    }

    fun continueOffline(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val existing = userProfileRepo.getProfile()
            if (existing == null) {
                userProfileRepo.saveProfile(
                    UserProfileEntity(
                        firebaseUid = "offline_athlete",
                        fullName = "Athlete",
                        email = "athlete@xtride.local",
                        photoUrl = null,
                        heightCm = 170f,
                        weightKg = 68f,
                        age = 25,
                        gender = "Not specified",
                        dailyStepGoal = 6000
                    )
                )
            }
            onSuccess()
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
                val existing = userProfileRepo.getProfile()
                if (existing == null) {
                    userProfileRepo.saveProfile(
                        UserProfileEntity(
                            firebaseUid = user.uid,
                            fullName = user.displayName.ifBlank { "Athlete" },
                            email = user.email,
                            photoUrl = user.photoUrl,
                            heightCm = 170f,
                            weightKg = 68f,
                            age = 25,
                            gender = "Not specified",
                            dailyStepGoal = 6000
                        )
                    )
                }
                _uiState.value = AuthUiState.Authenticated(user)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Google sign in failed")
            }
        }
    }
}