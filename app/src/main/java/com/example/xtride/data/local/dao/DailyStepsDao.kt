package com.example.xtride.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.xtride.data.local.entity.DailyStepsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStepsDao {
    @Query("SELECT * FROM daily_steps WHERE userId = :userId AND date = :todayDate LIMIT 1")
    fun observeTodaySteps(userId: String, todayDate: String): Flow<DailyStepsEntity?>

    @Query("SELECT * FROM daily_steps WHERE userId = :userId AND date = :todayDate LIMIT 1")
    suspend fun getTodayStepsDirect(userId: String, todayDate: String): DailyStepsEntity?

    @Query("SELECT * FROM daily_steps WHERE userId = :userId ORDER BY date DESC LIMIT 35")
    fun observeRecent35Days(userId: String): Flow<List<DailyStepsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailySteps(dailySteps: DailyStepsEntity)

    @Query("UPDATE daily_steps SET targetGoal = :newGoal WHERE userId = :userId AND date = :todayDate")
    suspend fun updateTodayGoal(userId: String, todayDate: String, newGoal: Int): Int
}