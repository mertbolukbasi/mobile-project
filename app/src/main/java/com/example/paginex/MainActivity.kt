package com.example.paginex

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

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
            val sharedPrefs = androidx.compose.runtime.remember { context.getSharedPreferences("paginex_settings", android.content.Context.MODE_PRIVATE) }
            var isDarkTheme by androidx.compose.runtime.remember { 
                androidx.compose.runtime.mutableStateOf(sharedPrefs.getBoolean("is_dark_theme", true)) 
            }
            
            LaunchedEffect(Unit) {
                FirestoreService.initializeData(forceReset = true) // Set to true to drop the db and write seed data unconditionally
                FirestoreService.syncMockData()
            }

            androidx.compose.runtime.CompositionLocalProvider(
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
}
