package com.example.xtride.domain.model

data class DailyStepSummary(
    val date: String,          // Format: "YYYY-MM-DD"
    val steps: Int,
    val targetGoal: Int = 6000,
    val distanceKm: Float,
    val caloriesBurned: Int,
    val activeMinutes: Int
) {
    val progressPercentage: Int
        get() = if (targetGoal > 0) ((steps.toFloat() / targetGoal) * 100).toInt().coerceAtMost(100) else 0
    val isGoalMet: Boolean
        get() = steps >= targetGoal
}