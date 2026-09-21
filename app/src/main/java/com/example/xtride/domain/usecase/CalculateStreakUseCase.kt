package com.example.xtride.domain.usecase
import kotlin.collections.map
import com.example.xtride.domain.model.StreakData
import com.example.xtride.domain.model.DailyStepSummary

class CalculateStreakUseCase {
    /**
     * Takes past daily summaries and computes:
     * 1. Current active streak
     * 2. Best streak achieved
     * 3. 35-day heatmap intensity list (0: <1.5k, 1: 1.5k-3.5k, 2: 3.5k-5k, 3: 5k-6k, 4: >=6k)
     */
    operator fun invoke(history: List<DailyStepSummary>, targetGoal: Int = 6000): StreakData {
        if (history.isEmpty()) {
            return StreakData(
                currentStreakDays = 0,
                bestStreakDays = 0,
                heatmapIntensities = List(35) { 0 }
            )
        }
        var currentStreak = 0
        var bestStreak = 0
        var tempStreak = 0

        // Iterate backwards from most recent day
        for (day in history) {
            if (day.steps >= targetGoal) {
                tempStreak++
                if (tempStreak > bestStreak) bestStreak = tempStreak
            } else {
                tempStreak = 0
            }
        }
        currentStreak = tempStreak

        // Map past 35 days to heatmap intensity levels (0 to 4)
        val recent35 = history.takeLast(35)
        val paddedList = if (recent35.size < 35) {
            List(35 - recent35.size) { 0 } + recent35.map { stepsToIntensity(it.steps, targetGoal) }
        } else {
            recent35.map { stepsToIntensity(it.steps, targetGoal) }
        }
        return StreakData(
            currentStreakDays = currentStreak,
            bestStreakDays = bestStreak.coerceAtLeast(currentStreak),
            heatmapIntensities = paddedList
        )
    }
    private fun stepsToIntensity(steps: Int, goal: Int): Int {
        return when {
            steps >= goal -> 4             // Goal Met
            steps >= (goal * 0.8f) -> 3    // High
            steps >= (goal * 0.5f) -> 2    // Medium
            steps >= (goal * 0.25f) -> 1   // Light
            else -> 0                      // Empty
        }
    }
}