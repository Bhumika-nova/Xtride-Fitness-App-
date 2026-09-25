package com.example.xtride.feature.analytics

enum class TimeRange(val title: String, val daysCount: Int) {
    WEEK("Week", 7),
    MONTH("Month", 30)
}

data class DailyStatItem(
    val date: String,
    val dayLabel: String,
    val stepsCount: Int,
    val targetGoal: Int,
    val distanceKm: Float,
    val caloriesBurned: Int,
    val isGoalMet: Boolean
)

data class AnalyticsUiState(
    val selectedRange: TimeRange = TimeRange.WEEK,
    val dailyStats: List<DailyStatItem> = emptyList(),
    val totalSteps: Int = 0,
    val averageDailySteps: Int = 0,
    val totalDistanceKm: Float = 0f,
    val totalCalories: Int = 0,
    val totalWorkoutsCount: Int = 0,
    val totalVolumeLiftedKg: Double = 0.0,
    val bestDaySteps: Int = 0,
    val bestDayDate: String = "",
    val selectedBarIndex: Int? = null
)
