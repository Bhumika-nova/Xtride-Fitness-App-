package com.example.xtride.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xtride.data.local.entity.UserProfileEntity
import com.example.xtride.data.repository.AuthRepository
import com.example.xtride.data.repository.UserProfileRepository
import com.example.xtride.domain.usecase.CalculateBmiUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class ProfileViewModel(
    private val userProfileRepo: UserProfileRepository,
    private val authRepo: AuthRepository? = null,
    private val calculateBmiUseCase: CalculateBmiUseCase = CalculateBmiUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        userProfileRepo.activeUserProfile
            .onEach { profile ->
                if (profile != null) {
                    val bmiResult = calculateBmiUseCase(profile.weightKg, profile.heightCm)
                    val (minWeight, maxWeight) = calculateHealthyWeightRange(profile.heightCm)
                    _uiState.value = _uiState.value.copy(
                        firebaseUid = profile.firebaseUid,
                        fullName = profile.fullName.ifEmpty { "Athlete" },
                        email = profile.email,
                        photoUrl = profile.photoUrl,
                        heightCm = profile.heightCm,
                        weightKg = profile.weightKg,
                        age = profile.age,
                        gender = profile.gender,
                        dailyStepGoal = profile.dailyStepGoal,
                        bmiScore = bmiResult.score,
                        bmiCategory = bmiResult.category,
                        healthyWeightMinKg = minWeight,
                        healthyWeightMaxKg = maxWeight
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateHeight(newHeightCm: Float) {
        val currentWeight = _uiState.value.weightKg
        val bmiResult = calculateBmiUseCase(currentWeight, newHeightCm)
        val (minWeight, maxWeight) = calculateHealthyWeightRange(newHeightCm)
        _uiState.value = _uiState.value.copy(
            heightCm = newHeightCm,
            bmiScore = bmiResult.score,
            bmiCategory = bmiResult.category,
            healthyWeightMinKg = minWeight,
            healthyWeightMaxKg = maxWeight
        )
    }

    fun updateWeight(newWeightKg: Float) {
        val currentHeight = _uiState.value.heightCm
        val bmiResult = calculateBmiUseCase(newWeightKg, currentHeight)
        _uiState.value = _uiState.value.copy(
            weightKg = newWeightKg,
            bmiScore = bmiResult.score,
            bmiCategory = bmiResult.category
        )
    }

    fun updateStepGoal(newGoal: Int) {
        _uiState.value = _uiState.value.copy(dailyStepGoal = newGoal)
    }

    fun updateAge(newAge: Int) {
        _uiState.value = _uiState.value.copy(age = newAge.coerceIn(12, 100))
    }

    fun updateGender(newGender: String) {
        _uiState.value = _uiState.value.copy(gender = newGender)
    }

    fun saveProfile() {
        val current = _uiState.value
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            userProfileRepo.saveProfile(
                UserProfileEntity(
                    firebaseUid = current.firebaseUid.ifEmpty { "local_athlete" },
                    fullName = current.fullName,
                    email = current.email,
                    photoUrl = current.photoUrl,
                    heightCm = current.heightCm,
                    weightKg = current.weightKg,
                    age = current.age,
                    gender = current.gender,
                    dailyStepGoal = current.dailyStepGoal,
                    updatedAt = System.currentTimeMillis()
                )
            )
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                snackbarMessage = "Profile updated successfully"
            )
        }
    }

    fun showLogoutDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showLogoutDialog = show)
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            userProfileRepo.logout()
            authRepo?.signOut()
            _uiState.value = _uiState.value.copy(showLogoutDialog = false)
            onLoggedOut()
        }
    }

    fun dismissSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    private fun calculateHealthyWeightRange(heightCm: Float): Pair<Float, Float> {
        if (heightCm <= 0f) return Pair(0f, 0f)
        val heightM = heightCm / 100f
        val hSquared = heightM * heightM
        val min = (18.5f * hSquared * 10).roundToInt() / 10f
        val max = (24.9f * hSquared * 10).roundToInt() / 10f
        return Pair(min, max)
    }
}
