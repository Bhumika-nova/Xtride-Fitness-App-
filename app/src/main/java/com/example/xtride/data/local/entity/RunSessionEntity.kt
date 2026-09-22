package com.example.xtride.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_sessions")
data class RunSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimeStamp: Long,
    val endTimeStamp: Long,
    val totalDistanceMeters: Float,
    val durationSeconds: Long,
    val avgPaceSecondsPerKm: Int,
    val totalCalories: Int,
    val elevationGainMeters: Float,
    val avgCadenceSpm: Int = 0           // Steps Per Minute from pocket accelerometer
)