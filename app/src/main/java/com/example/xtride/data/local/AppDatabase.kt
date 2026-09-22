package com.example.xtride.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.xtride.data.local.dao.DailyStepsDao
import com.example.xtride.data.local.dao.RunSessionDao
import com.example.xtride.data.local.dao.UserProfileDao
import com.example.xtride.data.local.dao.WorkoutDao
import com.example.xtride.data.local.entity.DailyStepsEntity
import com.example.xtride.data.local.entity.RoutePointEntity
import com.example.xtride.data.local.entity.RunSessionEntity
import com.example.xtride.data.local.entity.UserProfileEntity
import com.example.xtride.data.local.entity.WorkoutEntity
@Database(
    entities = [
        UserProfileEntity::class,
        DailyStepsEntity::class,
        WorkoutEntity::class,
        RunSessionEntity::class,
        RoutePointEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun dailyStepsDao(): DailyStepsDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun runSessionDao(): RunSessionDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "xtride_fitness_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}