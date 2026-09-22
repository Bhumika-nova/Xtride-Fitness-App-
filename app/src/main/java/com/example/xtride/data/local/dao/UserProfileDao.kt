package com.example.xtride.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.xtride.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getActiveUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET dailyStepGoal = :newGoal WHERE firebaseUid = :uid")
    suspend fun updateStepGoal(uid: String, newGoal: Int): Int

    @Query("UPDATE user_profile SET isDarkMode = :isDark WHERE firebaseUid = :uid")
    suspend fun updateThemePreference(uid: String, isDark: Boolean): Int

    @Query("DELETE FROM user_profile")
    suspend fun clearProfileOnLogout(): Int
}