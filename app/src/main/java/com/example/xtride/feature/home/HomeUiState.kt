package com.example.xtride.feature.home

import com.example.xtride.domain.model.StreakData

data class HeatmapDay(
    val date: String,
    val steps: Int,
    val goal: Int,
    val level: Int // 0 to 4 based on step completion
)

data class HomeUiState(
    val userName: String = "Athlete",
    val todaySteps: Int = 0,
    val dailyGoal: Int = 6000,
    val distanceKm: Float = 0f,
    val caloriesBurned: Int = 0,
    val activeMinutes: Int = 0,
    val streak: StreakData = StreakData(
        currentStreakDays = 0,
        bestStreakDays = 0,
        heatmapIntensities = List(35) { 0 }
    ),
    val heatmapDays: List<HeatmapDay> = emptyList(),
    val isLoading: Boolean = true
) {
    val progressPercentage: Float
        get() = if (dailyGoal > 0) (todaySteps.toFloat() / dailyGoal).coerceIn(0f, 1f) else 0f

    val percentDoneInt: Int
        get() = (progressPercentage * 100).toInt()
}