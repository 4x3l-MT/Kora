package com.ax3lmt.kora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ax3lmt.kora.ui.navigation.KoraNavigation
import com.ax3lmt.kora.ui.theme.KoraTheme
import com.ax3lmt.kora.viewmodel.KoraViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: KoraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appTheme by viewModel.appThemeState.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val useDarkTheme = when (appTheme) {
                "LIGHT" -> false
                "DARK" -> true
                else -> systemDark
            }

            KoraTheme(darkTheme = useDarkTheme, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KoraNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
