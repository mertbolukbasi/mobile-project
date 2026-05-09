package com.example.paginex

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*

import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    @androidx.compose.foundation.ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )

        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current

            val sharedPrefs = remember { context.getSharedPreferences("paginex_settings", android.content.Context.MODE_PRIVATE) }
            
            var currentUserId by remember { mutableStateOf(AuthService.getUid()) }
            
            DisposableEffect(Unit) {
                val authListener = FirebaseAuth.AuthStateListener { auth ->
                    currentUserId = auth.currentUser?.uid ?: ""
                }
                FirebaseAuth.getInstance().addAuthStateListener(authListener)
                onDispose {
                    FirebaseAuth.getInstance().removeAuthStateListener(authListener)
                }
            }

            val themeKey = if (currentUserId.isNotEmpty()) "is_dark_theme_$currentUserId" else "is_dark_theme"
            var isDarkTheme by remember(currentUserId) { 
                mutableStateOf(sharedPrefs.getBoolean(themeKey, true)) 
            }
            
            LaunchedEffect(Unit) {
                FirestoreService.initializeData(forceReset = false)
                FirestoreService.refreshSessionCacheFromFirestore()
            }

            DisposableEffect(Unit) {
                FirestoreService.attachUsersRealtimeListener()
                onDispose {
                    FirestoreService.detachUsersRealtimeListener()
                }
            }

            CompositionLocalProvider(
                LocalThemeToggle provides { 
                    val newTheme = !isDarkTheme
                    isDarkTheme = newTheme
                    sharedPrefs.edit().putBoolean(themeKey, newTheme).apply()
                },
                androidx.compose.foundation.LocalOverscrollConfiguration provides null
            ) {
                PaginexTheme(isDarkTheme = isDarkTheme) {
                    PaginexApp()
                }
            }
        }
    }
}
