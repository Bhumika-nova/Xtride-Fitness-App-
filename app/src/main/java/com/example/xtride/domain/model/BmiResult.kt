package com.example.xtride.domain.model

enum class BmiCategory(val label: String) {
    UNDERWEIGHT("Underweight"),
    NORMAL("Normal Weight"),
    OVERWEIGHT("Overweight"),
    OBESE("Obese")
}
data class BmiResult(
    val score: Float,
    val category: BmiCategory
)