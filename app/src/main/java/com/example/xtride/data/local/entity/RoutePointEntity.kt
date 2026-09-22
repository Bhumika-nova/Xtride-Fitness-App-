package com.example.xtride.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "route_points",
    foreignKeys = [
        ForeignKey(
            entity = RunSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class RoutePointEntity(
    @PrimaryKey(autoGenerate = true)
    val pointId: Long = 0,
    val sessionId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double = 0.0,
    val speedMps: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)