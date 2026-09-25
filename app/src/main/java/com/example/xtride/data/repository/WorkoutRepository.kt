package com.example.xtride.data.repository

import com.example.xtride.data.local.dao.WorkoutDao
import com.example.xtride.data.local.entity.WorkoutEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val userProfileRepo: UserProfileRepository? = null
) {
    private suspend fun getCurrentUserId(): String {
        return userProfileRepo?.getProfile()?.firebaseUid?.ifBlank { "offline_athlete" } ?: "offline_athlete"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val allWorkouts: Flow<List<WorkoutEntity>> = if (userProfileRepo != null) {
        userProfileRepo.activeUserProfile.flatMapLatest { profile ->
            val uid = profile?.firebaseUid?.ifBlank { "offline_athlete" } ?: "offline_athlete"
            workoutDao.getAllWorkouts(uid)
        }
    } else {
        workoutDao.getAllWorkouts("offline_athlete")
    }

    suspend fun logWorkout(workout: WorkoutEntity): Long {
        val uid = getCurrentUserId()
        return workoutDao.insertWorkout(workout.copy(userId = uid))
    }

    suspend fun deleteWorkout(workoutId: Long) {
        val uid = getCurrentUserId()
        workoutDao.deleteWorkoutById(uid, workoutId)
    }
}