package com.example.paginex

import android.content.Context
import android.util.Log
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

object AuthService {
    private val auth = FirebaseAuth.getInstance()
    private const val TAG = "AuthService"
    private const val PREFS_NAME = "paginex_auth"
    private const val KEY_EMAIL = "pending_email"

    fun getCurrentUser() = auth.currentUser
    fun isUserLoggedIn() = auth.currentUser != null
    fun getUid() = auth.currentUser?.uid ?: "u1"

    fun savePendingEmail(context: Context, email: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_EMAIL, email).apply()
    }

    fun getPendingEmail(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_EMAIL, null)
    }

    suspend fun sendSignInLink(context: Context, email: String): Result<Unit> {
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl("https://paginex.page.link/finishSignUp")
            .setHandleCodeInApp(true)
            .setAndroidPackageName("com.example.paginex", true, "24")
            .build()

        return try {
            auth.sendSignInLinkToEmail(email, actionCodeSettings).await()
            savePendingEmail(context, email)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending sign in link", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithEmailLink(email: String, link: String): Result<String> {
        return try {
            if (auth.isSignInWithEmailLink(link)) {
                val result = auth.signInWithEmailLink(email, link).await()
                val user = result.user
                if (user != null) {
                    val profile = FirestoreService.getUserById(user.uid)
                    if (profile == null) {
                        FirestoreService.createUserProfile(
                            userId = user.uid,
                            email = email,
                            name = "New",
                            surname = "User"
                        )
                    }
                    Result.success(user.uid)
                } else {
                    Result.failure(Exception("Sign in failed"))
                }
            } else {
                Result.failure(Exception("Invalid link"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error signing in", e)
            Result.failure(e)
        }
    }

    fun isSignInWithEmailLink(link: String): Boolean {
        return auth.isSignInWithEmailLink(link)
    }

    fun logout() {
        auth.signOut()
    }
}
