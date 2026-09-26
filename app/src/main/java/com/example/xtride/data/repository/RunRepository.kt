package com.example.xtride.data.repository

import com.example.xtride.data.local.dao.RunSessionDao
import com.example.xtride.data.local.entity.RoutePointEntity
import com.example.xtride.data.local.entity.RunSessionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class RunRepository(
    private val runSessionDao: RunSessionDao,
    private val userProfileRepo: UserProfileRepository? = null
) {
    private suspend fun getCurrentUserId(): String {
        return userProfileRepo?.getProfile()?.firebaseUid?.ifBlank { "offline_athlete" } ?: "offline_athlete"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val allRunSessions: Flow<List<RunSessionEntity>> = if (userProfileRepo != null) {
        userProfileRepo.activeUserProfile.flatMapLatest { profile ->
            val uid = profile?.firebaseUid?.ifBlank { "offline_athlete" } ?: "offline_athlete"
            runSessionDao.getAllRunSessions(uid)
        }
    } else {
        runSessionDao.getAllRunSessions("offline_athlete")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getRunSessionsByType(activityType: String): Flow<List<RunSessionEntity>> = if (userProfileRepo != null) {
        userProfileRepo.activeUserProfile.flatMapLatest { profile ->
            val uid = profile?.firebaseUid?.ifBlank { "offline_athlete" } ?: "offline_athlete"
            runSessionDao.getRunSessionsByType(uid, activityType)
        }
    } else {
        runSessionDao.getRunSessionsByType("offline_athlete", activityType)
    }

    fun getRoutePointsForSession(sessionId: Long): Flow<List<RoutePointEntity>> {
        return runSessionDao.getRoutePointsForSession(sessionId)
    }

    suspend fun saveRunSession(
        session: RunSessionEntity,
        routePoints: List<RoutePointEntity>
    ): Long {
        val uid = getCurrentUserId()
        val sessionId = runSessionDao.insertRunSession(session.copy(userId = uid))
        if (routePoints.isNotEmpty()) {
            val mappedPoints = routePoints.map { it.copy(sessionId = sessionId) }
            runSessionDao.insertRoutePoints(mappedPoints)
        }
        return sessionId
    }

    suspend fun deleteRunSession(sessionId: Long) {
        val uid = getCurrentUserId()
        runSessionDao.deleteRunSessionById(uid, sessionId)
    }
}
