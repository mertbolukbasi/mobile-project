package com.example.paginex

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var emailLinkState = mutableStateOf<String?>(null)

    @androidx.compose.foundation.ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )

        checkIntent(intent)

        setContent {
            val emailLink by remember { emailLinkState }
            val context = androidx.compose.ui.platform.LocalContext.current
            val scope = rememberCoroutineScope()

            val sharedPrefs = remember { context.getSharedPreferences("paginex_settings", android.content.Context.MODE_PRIVATE) }
            var isDarkTheme by remember { 
                mutableStateOf(sharedPrefs.getBoolean("is_dark_theme", true)) 
            }
            
            LaunchedEffect(Unit) {
                FirestoreService.initializeData(forceReset = false)
                FirestoreService.syncMockData()
            }

            // Handle email link
            LaunchedEffect(emailLink) {
                val link = emailLink
                if (link != null && AuthService.isSignInWithEmailLink(link)) {
                    val email = AuthService.getPendingEmail(context)
                    if (email != null) {
                        val result = AuthService.signInWithEmailLink(email, link)
                        if (result.isSuccess) {
                            FirestoreService.syncMockData()
                        }
                        emailLinkState.value = null
                    }
                }
            }

            CompositionLocalProvider(
                LocalThemeToggle provides { 
                    val newTheme = !isDarkTheme
                    isDarkTheme = newTheme
                    sharedPrefs.edit().putBoolean("is_dark_theme", newTheme).apply()
                },
                androidx.compose.foundation.LocalOverscrollConfiguration provides null
            ) {
                PaginexTheme(isDarkTheme = isDarkTheme) {
                    PaginexApp()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkIntent(intent)
    }

    private fun checkIntent(intent: Intent?) {
        val data = intent?.data?.toString()
        if (data != null) {
            emailLinkState.value = data
        }
    }
}
