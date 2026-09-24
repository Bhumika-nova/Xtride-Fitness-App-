package com.example.xtride.data.repository

import com.example.xtride.data.local.dao.DailyStepsDao
import com.example.xtride.data.local.entity.DailyStepsEntity
import kotlinx.coroutines.flow.Flow

class StepRepository(private val dailyStepsDao: DailyStepsDao) {
    fun observeTodaySteps(todayDate: String): Flow<DailyStepsEntity?> {
        return dailyStepsDao.observeTodaySteps(todayDate)
    }

    fun observeRecent35Days(): Flow<List<DailyStepsEntity>> {
        return dailyStepsDao.observeRecent35Days()
    }

    suspend fun getTodayStepsDirect(todayDate: String): DailyStepsEntity? {
        return dailyStepsDao.getTodayStepsDirect(todayDate)
    }

    suspend fun saveTodaySteps(dailySteps: DailyStepsEntity) {
        dailyStepsDao.insertOrUpdateDailySteps(dailySteps)
    }

    suspend fun updateTodayGoal(todayDate: String, newGoal: Int) {
        val existing = dailyStepsDao.getTodayStepsDirect(todayDate)
        if (existing == null) {
            dailyStepsDao.insertOrUpdateDailySteps(
                DailyStepsEntity(
                    date = todayDate,
                    stepsCount = 0,
                    rawSensorOffset = -1, // -1 denotes uncalibrated offset
                    targetGoal = newGoal
                )
            )
        } else {
            dailyStepsDao.updateTodayGoal(todayDate, newGoal)
        }
    }

    suspend fun resetTodaySteps(todayDate: String) {
        val existing = dailyStepsDao.getTodayStepsDirect(todayDate)
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
        val existing = dailyStepsDao.getTodayStepsDirect(todayDate)
        val updated = if (existing == null) {
            DailyStepsEntity(
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

    /**
     * Calibrates Android's Sensor.TYPE_STEP_COUNTER against the midnight / launch baseline.
     * Android returns the lifetime steps since the device turned on.
     * When uncalibrated (offset <= 0), offset is set to rawSensorSteps so today's steps start at 0.
     */
    suspend fun processRawSensorSteps(todayDate: String, rawSensorSteps: Int, targetGoal: Int): DailyStepsEntity {
        val existing = dailyStepsDao.getTodayStepsDirect(todayDate)
        val updated = if (existing == null || existing.rawSensorOffset <= 0) {
            // Calibrate baseline: rawSensorSteps is the starting count for today
            DailyStepsEntity(
                date = todayDate,
                stepsCount = 0,
                rawSensorOffset = rawSensorSteps,
                targetGoal = if (existing != null && existing.targetGoal > 0) existing.targetGoal else targetGoal,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
        } else {
            val stepsToday = if (rawSensorSteps < existing.rawSensorOffset) {
                // Device reboot detected: raw counter reset to 0
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