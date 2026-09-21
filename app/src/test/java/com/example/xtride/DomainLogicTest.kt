package com.example.xtride

import com.example.xtride.domain.model.BmiCategory
import com.example.xtride.domain.usecase.CalculateBmiUseCase
import com.example.xtride.domain.usecase.CalculateCaloriesAndDistanceUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class DomainLogicTest {
    private val calculateBmi = CalculateBmiUseCase()
    private val calculateMetrics = CalculateCaloriesAndDistanceUseCase()
    @Test
    fun bmiCalculation_isAccurate() {
        //  BMI 22.8
        val result = calculateBmi(weightKg = 64.5f, heightCm = 168f)
        assertEquals(22.9f, result.score, 0.1f)
        assertEquals(BmiCategory.NORMAL, result.category)
    }
    @Test
    fun stepMetrics_calculateCorrectDistanceAndCalories() {
        // 4382 steps give ~3.3 km, ~180 kcal
        val distance = calculateMetrics.calculateDistanceKm(4382)
        val calories = calculateMetrics.calculateCalories(4382)

        assertEquals(3.3f, distance, 0.2f)
        assertEquals(180, calories)
    }
}