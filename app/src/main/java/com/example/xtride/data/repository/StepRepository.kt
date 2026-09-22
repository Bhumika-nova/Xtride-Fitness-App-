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
    suspend fun saveTodaySteps(dailySteps: DailyStepsEntity) {
        dailyStepsDao.insertOrUpdateDailySteps(dailySteps)
    }
    suspend fun updateTodayGoal(todayDate: String, newGoal: Int) {
        dailyStepsDao.updateTodayGoal(todayDate, newGoal)
    }
}