package com.example.xtride.domain.usecase

import com.example.xtride.domain.model.DailyStepSummary
import com.example.xtride.domain.model.StreakData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalculateStreakUseCase {

    /**
     * Connects directly with the real-world calendar to compute:
     * 1. Active consecutive daily streak (walking backwards day-by-day along the calendar).
     * 2. Best streak achieved historically across consecutive calendar dates.
     * 3. Dynamic intensity levels (0 to 4) for the Crimson Heatmap.
     */
    operator fun invoke(
        history: List<DailyStepSummary>,
        targetGoal: Int = 6000,
        referenceDate: Date = Date()
    ): StreakData {
        if (history.isEmpty()) {
            return StreakData(
                currentStreakDays = 0,
                bestStreakDays = 0,
                heatmapIntensities = emptyList()
            )
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val historyMap = history.associateBy { it.date }

        val calendar = Calendar.getInstance()
        calendar.time = referenceDate
        val todayStr = sdf.format(calendar.time)

        // 1. Calculate Active Streak connected to real Calendar dates
        var currentStreak = 0
        val todaySummary = historyMap[todayStr]
        val todayMetGoal = (todaySummary?.steps ?: 0) >= (todaySummary?.targetGoal ?: targetGoal)

        if (todayMetGoal) {
            currentStreak++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            // Today is still in progress; check if yesterday kept the streak alive
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        // Walk backwards consecutively through calendar days
        while (true) {
            val dateStr = sdf.format(calendar.time)
            val record = historyMap[dateStr]
            val goal = record?.targetGoal ?: targetGoal
            if (record != null && record.steps >= goal) {
                currentStreak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        // 2. Calculate Best Streak across all calendar history
        var bestStreak = currentStreak
        var runningStreak = 0
        val sortedHistory = history.sortedBy { it.date }
        var prevCal: Calendar? = null

        for (item in sortedHistory) {
            val goal = if (item.targetGoal > 0) item.targetGoal else targetGoal
            val itemCal = Calendar.getInstance().apply {
                val parsed = sdf.parse(item.date)
                if (parsed != null) time = parsed
            }

            if (item.steps >= goal) {
                if (prevCal != null) {
                    val diffDays = (itemCal.timeInMillis - prevCal.timeInMillis) / (1000 * 60 * 60 * 24)
                    if (diffDays == 1L) {
                        runningStreak++
                    } else if (diffDays > 1L) {
                        runningStreak = 1
                    }
                } else {
                    runningStreak = 1
                }
                prevCal = itemCal
                if (runningStreak > bestStreak) bestStreak = runningStreak
            } else {
                runningStreak = 0
                prevCal = null
            }
        }

        val intensities = history.map { stepsToIntensity(it.steps, targetGoal) }

        return StreakData(
            currentStreakDays = currentStreak,
            bestStreakDays = bestStreak,
            heatmapIntensities = intensities
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