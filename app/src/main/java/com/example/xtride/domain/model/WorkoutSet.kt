package com.example.xtride.domain.model

data class WorkoutSet(
    val id: Long = 0,
    val exerciseName: String,
    val weightKg: Float,
    val reps: Int,
    val sets: Int,
    val timestamp: Long = System.currentTimeMillis()
)