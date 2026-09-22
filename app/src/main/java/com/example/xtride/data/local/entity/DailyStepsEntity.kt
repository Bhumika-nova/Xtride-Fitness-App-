package com.example.xtride.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "daily_steps")
data class DailyStepsEntity(
    @PrimaryKey
    val date: String,                   // Format: "YYYY-MM-DD"
    val stepsCount: Int,                // Today's computed steps
    val rawSensorOffset: Int,           // Sensor count at midnight or after reboot
    val targetGoal: Int = 0,
    val distanceKm: Float = 0f,
    val caloriesBurned: Int = 0,
    val activeMinutes: Int = 0,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)