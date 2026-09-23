package com.example.xtride.data.repository

import com.example.xtride.domain.model.AuthUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {
    val currentUser: AuthUser?
        get() = auth.currentUser?.let {
            AuthUser(
                uid = it.uid,
                email = it.email.orEmpty(),
                displayName = it.displayName ?: it.email?.substringBefore("@").orEmpty(),
                photoUrl = it.photoUrl?.toString()
            )
        }
    suspend fun loginWithEmail(email: String, pass: String): Result<AuthUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw Exception("User not found")
            Result.success(
                AuthUser(
                    uid = user.uid,
                    email = user.email.orEmpty(),
                    displayName = user.displayName ?: user.email?.substringBefore("@").orEmpty(),
                    photoUrl = user.photoUrl?.toString()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun signUpWithEmail(name: String, email: String, pass: String): Result<AuthUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw Exception("User registration failed")

            // Set displayName on Firebase profile
            val updateProfile = userProfileChangeRequest {
                displayName = name.trim()
            }
            user.updateProfile(updateProfile).await()
            Result.success(
                AuthUser(
                    uid = user.uid,
                    email = user.email.orEmpty(),
                    displayName = name.trim(),
                    photoUrl = null
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun signOut() {
        auth.signOut()
    }
// google login
    suspend fun loginWithGoogle(idToken: String): Result<AuthUser> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Google sign in failed")
            Result.success(
                AuthUser(
                    uid = user.uid,
                    email = user.email.orEmpty(),
                    displayName = user.displayName ?: user.email?.substringBefore("@").orEmpty(),
                    photoUrl = user.photoUrl?.toString()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}