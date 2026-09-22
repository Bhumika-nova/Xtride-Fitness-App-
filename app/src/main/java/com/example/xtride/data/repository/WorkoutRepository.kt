package com.example.xtride.data.repository

import com.example.xtride.data.local.dao.WorkoutDao
import com.example.xtride.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow


class WorkoutRepository(private val workoutDao: WorkoutDao) {
    val allWorkouts: Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()
    suspend fun logWorkout(workout: WorkoutEntity): Long {
        return workoutDao.insertWorkout(workout)
    }
    suspend fun deleteWorkout(workoutId: Long) {
        workoutDao.deleteWorkoutById(workoutId)
    }
}