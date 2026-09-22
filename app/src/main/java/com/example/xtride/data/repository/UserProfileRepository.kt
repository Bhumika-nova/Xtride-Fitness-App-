package com.example.xtride.data.repository

import com.example.xtride.data.local.dao.UserProfileDao
import com.example.xtride.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(private val userProfileDao: UserProfileDao) {
    val activeUserProfile: Flow<UserProfileEntity?> = userProfileDao.getActiveUserProfile()
    suspend fun saveProfile(profile: UserProfileEntity) {
        userProfileDao.insertOrUpdateProfile(profile)
    }
    suspend fun updateGoal(uid: String, newGoal: Int) {
        userProfileDao.updateStepGoal(uid, newGoal)
    }
    suspend fun updateTheme(uid: String, isDark: Boolean) {
        userProfileDao.updateThemePreference(uid, isDark)
    }
    suspend fun logout() {
        userProfileDao.clearProfileOnLogout()
    }
}