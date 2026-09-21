package com.example.xtride.domain.usecase
import kotlin.math.roundToInt
import com.example.xtride.domain.model.BmiCategory
import com.example.xtride.domain.model.BmiResult

class CalculateBmiUseCase {
    operator fun invoke(weightKg: Float, heightCm: Float): BmiResult {
        if (heightCm <= 0f || weightKg <= 0f) {
            return BmiResult(score = 0f, category = BmiCategory.NORMAL)
        }
        val heightMeters = heightCm / 100f
        val rawBmi = weightKg / (heightMeters * heightMeters)
        // Round to 1 decimal place
        val roundedBmi = (rawBmi * 10).roundToInt() / 10f
        val category = when {
            roundedBmi < 18.5f -> BmiCategory.UNDERWEIGHT
            roundedBmi in 18.5f..24.9f -> BmiCategory.NORMAL
            roundedBmi in 25.0f..29.9f -> BmiCategory.OVERWEIGHT
            else -> BmiCategory.OBESE
        }
        return BmiResult(score = roundedBmi, category = category)
    }
}
