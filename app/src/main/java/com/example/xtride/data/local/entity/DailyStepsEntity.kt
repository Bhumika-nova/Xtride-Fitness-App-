package com.example.xtride.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "daily_steps",
    primaryKeys = ["userId", "date"]
)
data class DailyStepsEntity(
    val userId: String = "offline_athlete", // Scoped to individual user account
    val date: String,                       // Format: "YYYY-MM-DD"
    val stepsCount: Int,                    // Today's computed steps
    val rawSensorOffset: Int,               // Sensor count at midnight or after reboot
    val targetGoal: Int = 0,
    val distanceKm: Float = 0f,
    val caloriesBurned: Int = 0,
    val activeMinutes: Int = 0,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)