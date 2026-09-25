package com.example.xtride.data.repository

import com.example.xtride.data.local.dao.DailyStepsDao
import com.example.xtride.data.local.entity.DailyStepsEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class StepRepository(
    private val dailyStepsDao: DailyStepsDao,
    private val userProfileRepo: UserProfileRepository? = null
) {
    private suspend fun getCurrentUserId(): String {
        return userProfileRepo?.getProfile()?.firebaseUid?.ifBlank { "offline_athlete" } ?: "offline_athlete"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeTodaySteps(todayDate: String): Flow<DailyStepsEntity?> {
        return if (userProfileRepo != null) {
            userProfileRepo.activeUserProfile.flatMapLatest { profile ->
                val uid = profile?.firebaseUid?.ifBlank { "offline_athlete" } ?: "offline_athlete"
                dailyStepsDao.observeTodaySteps(uid, todayDate)
            }
        } else {
            dailyStepsDao.observeTodaySteps("offline_athlete", todayDate)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeRecent35Days(): Flow<List<DailyStepsEntity>> {
        return if (userProfileRepo != null) {
            userProfileRepo.activeUserProfile.flatMapLatest { profile ->
                val uid = profile?.firebaseUid?.ifBlank { "offline_athlete" } ?: "offline_athlete"
                dailyStepsDao.observeRecent35Days(uid)
            }
        } else {
            dailyStepsDao.observeRecent35Days("offline_athlete")
        }
    }

    suspend fun getTodayStepsDirect(todayDate: String): DailyStepsEntity? {
        val uid = getCurrentUserId()
        return dailyStepsDao.getTodayStepsDirect(uid, todayDate)
    }

    suspend fun saveTodaySteps(dailySteps: DailyStepsEntity) {
        val uid = getCurrentUserId()
        dailyStepsDao.insertOrUpdateDailySteps(dailySteps.copy(userId = uid))
    }

    suspend fun updateTodayGoal(todayDate: String, newGoal: Int) {
        val uid = getCurrentUserId()
        val existing = dailyStepsDao.getTodayStepsDirect(uid, todayDate)
        if (existing == null) {
            dailyStepsDao.insertOrUpdateDailySteps(
                DailyStepsEntity(
                    userId = uid,
                    date = todayDate,
                    stepsCount = 0,
                    rawSensorOffset = -1, // -1 denotes uncalibrated offset
                    targetGoal = newGoal
                )
            )
        } else {
            dailyStepsDao.updateTodayGoal(uid, todayDate, newGoal)
        }
    }

    suspend fun resetTodaySteps(todayDate: String) {
        val uid = getCurrentUserId()
        val existing = dailyStepsDao.getTodayStepsDirect(uid, todayDate)
        if (existing != null) {
            dailyStepsDao.insertOrUpdateDailySteps(
                existing.copy(
                    stepsCount = 0,
                    rawSensorOffset = -1, // Reset to uncalibrated so next sensor event captures fresh baseline
                    distanceKm = 0f,
                    caloriesBurned = 0,
                    activeMinutes = 0,
                    lastUpdatedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun addSteps(todayDate: String, increment: Int, goal: Int = 6000): DailyStepsEntity {
        val uid = getCurrentUserId()
        val existing = dailyStepsDao.getTodayStepsDirect(uid, todayDate)
        val updated = if (existing == null) {
            DailyStepsEntity(
                userId = uid,
                date = todayDate,
                stepsCount = increment,
                rawSensorOffset = -1,
                targetGoal = goal
            )
        } else {
            existing.copy(
                stepsCount = existing.stepsCount + increment,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
        }
        dailyStepsDao.insertOrUpdateDailySteps(updated)
        return updated
    }

    suspend fun processRawSensorSteps(todayDate: String, rawSensorSteps: Int, targetGoal: Int): DailyStepsEntity {
        val uid = getCurrentUserId()
        val existing = dailyStepsDao.getTodayStepsDirect(uid, todayDate)
        val updated = if (existing == null || existing.rawSensorOffset <= 0) {
            // Calibrate baseline: rawSensorSteps is the starting count for today for this user
            DailyStepsEntity(
                userId = uid,
                date = todayDate,
                stepsCount = 0,
                rawSensorOffset = rawSensorSteps,
                targetGoal = if (existing != null && existing.targetGoal > 0) existing.targetGoal else targetGoal,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
        } else {
            val stepsToday = if (rawSensorSteps < existing.rawSensorOffset) {
                existing.stepsCount + rawSensorSteps
            } else {
                rawSensorSteps - existing.rawSensorOffset
            }
            existing.copy(
                stepsCount = stepsToday,
                targetGoal = if (existing.targetGoal > 0) existing.targetGoal else targetGoal,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
        }
        dailyStepsDao.insertOrUpdateDailySteps(updated)
        return updated
    }
}