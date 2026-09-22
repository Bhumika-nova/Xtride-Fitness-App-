package com.example.xtride.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.xtride.data.local.entity.RoutePointEntity
import com.example.xtride.data.local.entity.RunSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RunSessionDao {
    @Query("SELECT * FROM run_sessions ORDER BY startTimeStamp DESC")
    fun getAllRunSessions(): Flow<List<RunSessionEntity>>

    @Query("SELECT * FROM route_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getRoutePointsForSession(sessionId: Long): Flow<List<RoutePointEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRunSession(session: RunSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutePoints(points: List<RoutePointEntity>)
}