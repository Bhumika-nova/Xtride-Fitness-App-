package com.example.xtride.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xtride.data.local.entity.DailyStepsEntity
import com.example.xtride.data.local.entity.WorkoutEntity
import com.example.xtride.data.repository.StepRepository
import com.example.xtride.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AnalyticsViewModel(
    private val stepRepo: StepRepository,
    private val workoutRepo: WorkoutRepository
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(TimeRange.WEEK)
    private val _selectedBarIndex = MutableStateFlow<Int?>(null)

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.US)
    private val dayOfMonthFormat = SimpleDateFormat("d", Locale.US)

    init {
        combine(
            stepRepo.observeRecent35Days(),
            workoutRepo.allWorkouts,
            _selectedRange,
            _selectedBarIndex
        ) { stepsList, workoutList, range, selectedIndex ->
            computeAnalytics(stepsList, workoutList, range, selectedIndex)
        }.launchIn(viewModelScope)
    }

    fun setTimeRange(range: TimeRange) {
        _selectedRange.value = range
        _selectedBarIndex.value = null
    }

    fun selectBar(index: Int?) {
        _selectedBarIndex.value = if (_selectedBarIndex.value == index) null else index
    }

    private fun computeAnalytics(
        recentSteps: List<DailyStepsEntity>,
        allWorkouts: List<WorkoutEntity>,
        range: TimeRange,
        selectedBarIndex: Int?
    ) {
        val stepsByDate = recentSteps.associateBy { it.date }
        val daysCount = range.daysCount
        val dailyStats = mutableListOf<DailyStatItem>()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Generate past 'daysCount' days ending today
        calendar.add(Calendar.DAY_OF_YEAR, -(daysCount - 1))

        var totalSteps = 0
        var totalDistance = 0f
        var totalCalories = 0
        var bestDaySteps = 0
        var bestDayDate = ""

        for (i in 0 until daysCount) {
            val dateStr = dateFormat.format(calendar.time)
            val label = if (range == TimeRange.WEEK) {
                dayOfWeekFormat.format(calendar.time)
            } else {
                dayOfMonthFormat.format(calendar.time)
            }

            val entity = stepsByDate[dateStr]
            val steps = entity?.stepsCount ?: 0
            val goal = entity?.targetGoal ?: 6000
            val distance = entity?.distanceKm ?: (steps * 0.00075f)
            val calories = entity?.caloriesBurned ?: (steps * 0.04f).toInt()
            val isGoalMet = steps >= goal && steps > 0

            dailyStats.add(
                DailyStatItem(
                    date = dateStr,
                    dayLabel = label,
                    stepsCount = steps,
                    targetGoal = goal,
                    distanceKm = distance,
                    caloriesBurned = calories,
                    isGoalMet = isGoalMet
                )
            )

            totalSteps += steps
            totalDistance += distance
            totalCalories += calories

            if (steps > bestDaySteps) {
                bestDaySteps = steps
                bestDayDate = dateStr
            }

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val avgSteps = if (daysCount > 0) totalSteps / daysCount else 0

        // Compute workout stats for this timeframe
        val cutoffTimestamp = System.currentTimeMillis() - (daysCount.toLong() * 24L * 60L * 60L * 1000L)
        val relevantWorkouts = allWorkouts.filter { it.timestamp >= cutoffTimestamp }
        val totalWorkoutsCount = relevantWorkouts.size
        val totalVolume = relevantWorkouts.sumOf { (it.weightKg * it.reps * it.sets).toDouble() }

        _uiState.value = AnalyticsUiState(
            selectedRange = range,
            dailyStats = dailyStats,
            totalSteps = totalSteps,
            averageDailySteps = avgSteps,
            totalDistanceKm = totalDistance,
            totalCalories = totalCalories,
            totalWorkoutsCount = totalWorkoutsCount,
            totalVolumeLiftedKg = totalVolume,
            bestDaySteps = bestDaySteps,
            bestDayDate = bestDayDate,
            selectedBarIndex = selectedBarIndex
        )
    }
}
