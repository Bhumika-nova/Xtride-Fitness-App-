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
    @Query("SELECT * FROM run_sessions WHERE userId = :userId ORDER BY startTimeStamp DESC")
    fun getAllRunSessions(userId: String): Flow<List<RunSessionEntity>>

    @Query("SELECT * FROM run_sessions WHERE userId = :userId AND activityType = :activityType ORDER BY startTimeStamp DESC")
    fun getRunSessionsByType(userId: String, activityType: String): Flow<List<RunSessionEntity>>

    @Query("SELECT * FROM run_sessions WHERE userId = :userId AND id = :sessionId LIMIT 1")
    suspend fun getRunSessionById(userId: String, sessionId: Long): RunSessionEntity?

    @Query("SELECT * FROM route_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getRoutePointsForSession(sessionId: Long): Flow<List<RoutePointEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRunSession(session: RunSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutePoints(points: List<RoutePointEntity>)

    @Query("DELETE FROM run_sessions WHERE userId = :userId AND id = :sessionId")
    suspend fun deleteRunSessionById(userId: String, sessionId: Long)
}