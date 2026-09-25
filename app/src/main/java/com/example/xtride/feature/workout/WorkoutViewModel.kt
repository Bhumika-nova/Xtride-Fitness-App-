package com.example.xtride.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xtride.data.local.entity.WorkoutEntity
import com.example.xtride.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkoutViewModel(
    private val workoutRepo: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        observeWorkoutHistory()
    }

    private fun observeWorkoutHistory() {
        viewModelScope.launch {
            workoutRepo.allWorkouts.collect { list ->
                _uiState.update { it.copy(historyList = list) }
            }
        }
    }

    fun selectTab(tab: WorkoutTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectExercise(exercise: String) {
        _uiState.update { it.copy(selectedExercise = exercise) }
    }

    fun updateCustomExerciseName(name: String) {
        _uiState.update { it.copy(customExerciseName = name) }
    }

    fun adjustWeight(delta: Float) {
        _uiState.update { state ->
            val newWeight = (state.weightKg + delta).coerceIn(0f, 500f)
            // Round to 1 decimal place (e.g., 62.5 kg)
            state.copy(weightKg = Math.round(newWeight * 10f) / 10f)
        }
    }

    fun adjustReps(delta: Int) {
        _uiState.update { state ->
            val newReps = (state.reps + delta).coerceIn(1, 100)
            state.copy(reps = newReps)
        }
    }

    fun adjustSets(delta: Int) {
        _uiState.update { state ->
            val newSets = (state.sets + delta).coerceIn(1, 25)
            state.copy(sets = newSets)
        }
    }

    fun saveCurrentWorkout() {
        val state = _uiState.value
        val exerciseName = state.effectiveExerciseName

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val entity = WorkoutEntity(
                exerciseName = exerciseName,
                weightKg = state.weightKg,
                reps = state.reps,
                sets = state.sets,
                timestamp = System.currentTimeMillis()
            )
            workoutRepo.logWorkout(entity)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    snackbarMessage = "Logged $exerciseName successfully! 🏋️"
                )
            }
        }
    }

    fun deleteWorkout(workoutId: Long) {
        viewModelScope.launch {
            workoutRepo.deleteWorkout(workoutId)
        }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
