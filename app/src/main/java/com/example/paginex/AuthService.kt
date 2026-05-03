package com.example.paginex

import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

object AuthService {
    private val auth = FirebaseAuth.getInstance()
    private const val TAG = "AuthService"

    fun getCurrentUser() = auth.currentUser
    fun isUserLoggedIn() = auth.currentUser != null
    fun getUid() = auth.currentUser?.uid ?: "u1"
    fun getUserEmail() = auth.currentUser?.email ?: ""

    fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified ?: false
    }

    suspend fun reloadUser(): Boolean {
        return try {
            auth.currentUser?.reload()?.await()
            auth.currentUser?.isEmailVerified ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error reloading user", e)
            false
        }
    }

    suspend fun sendVerificationEmail(): Result<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending verification email", e)
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Send verification email
                user.sendEmailVerification().await()
                // Create user profile in Firestore
                FirestoreService.createUserProfile(
                    userId = user.uid,
                    email = email,
                    name = "New",
                    surname = "User"
                )
                Result.success(user.uid)
            } else {
                Result.failure(Exception("Sign up failed"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error signing up", e)
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Reload to get latest emailVerified status
                user.reload().await()
                if (!user.isEmailVerified) {
                    Result.failure(Exception("EMAIL_NOT_VERIFIED"))
                } else {
                    Result.success(user.uid)
                }
            } else {
                Result.failure(Exception("Sign in failed"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error signing in", e)
            Result.failure(e)
        }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val email = user.email ?: return Result.failure(Exception("No email found"))
            // Re-authenticate first
            val credential = EmailAuthProvider.getCredential(email, oldPassword)
            user.reauthenticate(credential).await()
            // Update password
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error changing password", e)
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(password: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val email = user.email ?: return Result.failure(Exception("No email found"))
            
            // Re-authenticate first
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            
            // Perform soft delete in Firestore
            val success = FirestoreService.deleteUserData(user.uid)
            if (!success) {
                return Result.failure(Exception("Failed to clean up user data"))
            }
            
            // Delete the account from Firebase Auth
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting account", e)
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
