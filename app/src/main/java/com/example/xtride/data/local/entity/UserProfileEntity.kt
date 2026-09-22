package com.example.xtride.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val firebaseUid: String,            // Unique ID from Firebase Auth
    val fullName: String,               // Google Account
    val email: String,                  // Real email from Firebase Auth
    val photoUrl: String? = null,       // Profile photo from Google Account
    val heightCm: Float,                // Set during Onboarding
    val weightKg: Float,                // Set during Onboarding
    val age: Int,                       // Set during Onboarding
    val gender: String,                 // Set during Onboarding
    val dailyStepGoal: Int = 0,      // Daily step goal
    val isDarkMode: Boolean = false,    // Theme preference
    val updatedAt: Long = System.currentTimeMillis()
)