package com.example.paginex

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
            var isDarkTheme by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            
            androidx.compose.runtime.CompositionLocalProvider(
                LocalThemeToggle provides { isDarkTheme = !isDarkTheme },
                androidx.compose.foundation.LocalOverscrollConfiguration provides null
            ) {
                PaginexTheme(isDarkTheme = isDarkTheme) {
                    PaginexApp()
                }
            }
        }
    }
}
