package com.example.xtride.feature.workout

import com.example.xtride.data.local.entity.WorkoutEntity

enum class WorkoutTab {
    LOG,
    HISTORY
}

data class WorkoutUiState(
    val selectedTab: WorkoutTab = WorkoutTab.LOG,
    val selectedExercise: String = "Bench Press",
    val customExerciseName: String = "",
    val weightKg: Float = 60.0f,
    val reps: Int = 10,
    val sets: Int = 3,
    val historyList: List<WorkoutEntity> = emptyList(),
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null
) {
    val effectiveExerciseName: String
        get() = if (selectedExercise == "Custom") {
            customExerciseName.ifBlank { "Custom Exercise" }
        } else {
            selectedExercise
        }

    val totalVolumeKg: Float
        get() = weightKg * reps * sets
}