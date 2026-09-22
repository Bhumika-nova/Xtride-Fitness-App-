package com.example.xtride.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_records")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseName: String,
    val weightKg: Float,
    val reps: Int,
    val sets: Int,
    val timestamp: Long = System.currentTimeMillis()
)