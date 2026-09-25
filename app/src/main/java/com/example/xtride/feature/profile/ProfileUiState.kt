package com.example.xtride.feature.profile

import com.example.xtride.domain.model.BmiCategory

data class ProfileUiState(
    val firebaseUid: String = "",
    val fullName: String = "Athlete",
    val email: String = "",
    val photoUrl: String? = null,
    val heightCm: Float = 175f,
    val weightKg: Float = 70f,
    val age: Int = 25,
    val gender: String = "Not specified",
    val dailyStepGoal: Int = 6000,
    val bmiScore: Float = 22.9f,
    val bmiCategory: BmiCategory = BmiCategory.NORMAL,
    val healthyWeightMinKg: Float = 56.7f,
    val healthyWeightMaxKg: Float = 76.3f,
    val isSaving: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val snackbarMessage: String? = null
)
