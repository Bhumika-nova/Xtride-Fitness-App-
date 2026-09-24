package com.example.xtride.data.repository

import com.example.xtride.data.local.dao.UserProfileDao
import com.example.xtride.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class UserProfileRepository(private val userProfileDao: UserProfileDao) {
    val activeUserProfile: Flow<UserProfileEntity?> = userProfileDao.getActiveUserProfile()

    suspend fun getProfile(): UserProfileEntity? {
        return activeUserProfile.firstOrNull()
    }

    suspend fun saveProfile(profile: UserProfileEntity) {
        userProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun updateGoal(uid: String, newGoal: Int) {
        userProfileDao.updateStepGoal(uid, newGoal)
    }

    suspend fun updateDailyGoal(newGoal: Int) {
        val current = getProfile()
        if (current == null) {
            saveProfile(
                UserProfileEntity(
                    firebaseUid = "local_athlete",
                    fullName = "Athlete",
                    email = "",
                    heightCm = 170f,
                    weightKg = 68f,
                    age = 25,
                    gender = "Not specified",
                    dailyStepGoal = newGoal
                )
            )
        } else {
            userProfileDao.updateStepGoal(current.firebaseUid, newGoal)
        }
    }

    suspend fun updateTheme(uid: String, isDark: Boolean) {
        userProfileDao.updateThemePreference(uid, isDark)
    }

    suspend fun logout() {
        userProfileDao.clearProfileOnLogout()
    }
}