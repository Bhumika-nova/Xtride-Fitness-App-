package com.example.xtride.domain.usecase

import kotlin.math.roundToInt

data class StepMetrics(
    val distanceKm: Float,
    val caloriesBurned: Int,
    val activeMinutes: Int
)

class CalculateCaloriesAndDistanceUseCase {

    operator fun invoke(steps: Int, heightCm: Float = 170f, weightKg: Float = 68f): StepMetrics {
        val strideMeters = (heightCm * 0.414f) / 100f
        val km = calculateDistanceKm(steps, strideMeters)
        val cal = calculateCalories(steps)
        val mins = calculateActiveMinutes(steps)
        return StepMetrics(km, cal, mins)
    }

    // Converts steps into estimated distance in km
    fun calculateDistanceKm(steps: Int, strideLengthMeters: Float = 0.762f): Float {
        val totalMeters = steps * strideLengthMeters
        val km = totalMeters / 1000f
        return (km * 10).roundToInt() / 10f // e.g. 3.1 km
    }

    // Estimates calories burned
    fun calculateCalories(steps: Int, caloriesPerStep: Float = 0.041f): Int {
        return (steps * caloriesPerStep).roundToInt()
    }

    // Estimates active moving minutes
    fun calculateActiveMinutes(steps: Int, stepsPerMinute: Int = 115): Int {
        return (steps / stepsPerMinute).coerceAtLeast(0)
    }
}