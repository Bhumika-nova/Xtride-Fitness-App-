package com.example.xtride.domain.usecase
import kotlin.math.roundToInt

class CalculateCaloriesAndDistanceUseCase {


     //Converts steps into estimated distance in km
    fun calculateDistanceKm(steps: Int, strideLengthMeters: Float = 0.762f): Float {
        val totalMeters = steps * strideLengthMeters
        val km = totalMeters / 1000f
        return (km * 10).roundToInt() / 10f // e.g. 3.1 km
    }


     //Estimates calories burned
    fun calculateCalories(steps: Int, caloriesPerStep: Float = 0.041f): Int {
        return (steps * caloriesPerStep).roundToInt()
    }


     // Estimates active moving minutes
    fun calculateActiveMinutes(steps: Int, stepsPerMinute: Int = 115): Int {
        return (steps / stepsPerMinute).coerceAtLeast(0)
    }
}