package com.example.xtride.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xtride.data.local.entity.DailyStepsEntity
import com.example.xtride.data.repository.StepRepository
import com.example.xtride.data.repository.UserProfileRepository
import com.example.xtride.data.sensor.StepSensorManager
import com.example.xtride.domain.model.DailyStepSummary
import com.example.xtride.domain.usecase.CalculateCaloriesAndDistanceUseCase
import com.example.xtride.domain.usecase.CalculateStreakUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeViewModel(
    private val stepRepo: StepRepository,
    private val userProfileRepo: UserProfileRepository,
    private val sensorManager: StepSensorManager?,
    private val calculateStreakUseCase: CalculateStreakUseCase = CalculateStreakUseCase(),
    private val calculateMetricsUseCase: CalculateCaloriesAndDistanceUseCase = CalculateCaloriesAndDistanceUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeUserProfile()
        observeTodaySteps()
        observePastHistory()
        initHardwareSensor()
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            userProfileRepo.activeUserProfile.collect { profile ->
                profile?.let {
                    _uiState.update { state ->
                        state.copy(
                            userName = it.fullName.ifBlank { "Athlete" },
                            dailyGoal = it.dailyStepGoal
                        )
                    }
                }
            }
        }
    }

    private var todayStepsJob: kotlinx.coroutines.Job? = null
    private var currentObservedDate = ""

    private fun observeTodaySteps() {
        val todayStr = getTodayDateString()
        if (todayStr == currentObservedDate && todayStepsJob?.isActive == true) return
        currentObservedDate = todayStr
        todayStepsJob?.cancel()

        todayStepsJob = viewModelScope.launch {
            stepRepo.observeTodaySteps(todayStr).collect { record ->
                val steps = record?.stepsCount ?: 0
                val profile = userProfileRepo.getProfile()
                val heightCm = profile?.heightCm ?: 170f
                val weightKg = profile?.weightKg ?: 68f

                val metrics = calculateMetricsUseCase(
                    steps = steps,
                    heightCm = heightCm,
                    weightKg = weightKg
                )

                _uiState.update { state ->
                    state.copy(
                        todaySteps = steps,
                        distanceKm = metrics.distanceKm,
                        caloriesBurned = metrics.caloriesBurned,
                        activeMinutes = metrics.activeMinutes,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observePastHistory() {
        viewModelScope.launch {
            stepRepo.observeRecent35Days().collect { history ->
                val summaries = history.map { entity ->
                    DailyStepSummary(
                        date = entity.date,
                        steps = entity.stepsCount,
                        targetGoal = entity.targetGoal,
                        distanceKm = entity.distanceKm,
                        caloriesBurned = entity.caloriesBurned,
                        activeMinutes = entity.activeMinutes
                    )
                }
                val streak = calculateStreakUseCase(summaries, _uiState.value.dailyGoal)
                val heatmap = generateHeatmapDays(history)

                _uiState.update { state ->
                    state.copy(
                        streak = streak,
                        heatmapDays = heatmap
                    )
                }
            }
        }
    }

    private fun initHardwareSensor() {
        sensorManager?.let { manager ->
            viewModelScope.launch {
                manager.getRawStepCountFlow().collect { rawSteps ->
                    val todayStr = getTodayDateString()
                    if (todayStr != currentObservedDate) {
                        // Date changed at midnight! Re-bind to the new day automatically
                        observeTodaySteps()
                    }
                    val profile = userProfileRepo.getProfile()
                    val goal = profile?.dailyStepGoal ?: 6000
                    stepRepo.processRawSensorSteps(todayStr, rawSteps, goal)
                }
            }
        }
    }

    private fun generateHeatmapDays(history: List<DailyStepsEntity>, daysCount: Int = 35): List<HeatmapDay> {
        val map = history.associateBy { it.date }
        val days = mutableListOf<HeatmapDay>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance()

        for (i in (daysCount - 1) downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = sdf.format(calendar.time)
            val record = map[dateStr]
            val steps = record?.stepsCount ?: 0
            val goal = record?.targetGoal ?: 6000

            val level = when {
                steps >= goal -> 4
                steps >= (goal * 0.75).toInt() -> 3
                steps >= (goal * 0.50).toInt() -> 2
                steps >= (goal * 0.25).toInt() -> 1
                else -> 0
            }

            days.add(HeatmapDay(date = dateStr, steps = steps, goal = goal, level = level))
        }
        return days
    }

    fun updateDailyGoal(newGoal: Int) {
        _uiState.update { it.copy(dailyGoal = newGoal) }
        viewModelScope.launch {
            userProfileRepo.updateDailyGoal(newGoal)
            val todayStr = getTodayDateString()
            stepRepo.updateTodayGoal(todayStr, newGoal)
        }
    }

    fun simulateSteps(count: Int = 150) {
        viewModelScope.launch {
            val todayStr = getTodayDateString()
            stepRepo.addSteps(todayStr, count, _uiState.value.dailyGoal)
        }
    }

    fun resetTodaySteps() {
        viewModelScope.launch {
            val todayStr = getTodayDateString()
            stepRepo.resetTodaySteps(todayStr)
        }
    }
}