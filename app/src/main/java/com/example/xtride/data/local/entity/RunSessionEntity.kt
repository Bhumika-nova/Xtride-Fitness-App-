package com.example.xtride.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_sessions")
data class RunSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "offline_athlete",
    val activityType: String = "RUN",    // "RUN", "WALK", "HIKE"
    val startTimeStamp: Long,
    val endTimeStamp: Long,
    val totalDistanceMeters: Float,
    val durationSeconds: Long,
    val avgPaceSecondsPerKm: Int,
    val totalCalories: Int,
    val elevationGainMeters: Float = 0f,
    val avgCadenceSpm: Int = 0           // Steps Per Minute from pocket accelerometer
)