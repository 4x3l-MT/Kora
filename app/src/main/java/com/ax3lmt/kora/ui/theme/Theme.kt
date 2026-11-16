package com.ax3lmt.kora.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFD81B60),
    onPrimary = Color.White,
    primaryContainer = LightPastelPink,
    onPrimaryContainer = Color(0xFF500018),
    secondary = LightSkyBlue,
    onSecondary = Color(0xFF003254),
    secondaryContainer = LightPastelGreen,
    onSecondaryContainer = Color(0xFF183300),
    tertiary = LightSoftAqua,
    onTertiary = Color(0xFF00372E),
    background = LightCream,
    onBackground = Color(0xFF1F1B13),
    surface = LightCream,
    onSurface = Color(0xFF1F1B13),
    surfaceVariant = LightSoftAqua.copy(alpha = 0.5f),
    onSurfaceVariant = Color(0xFF00372E)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF48FB1),
    onPrimary = Color(0xFF560027),
    primaryContainer = DarkDeepPlum,
    onPrimaryContainer = Color(0xFFFCD7FA),
    secondary = Color(0xFF90CAF9),
    onSecondary = Color(0xFF00344D),
    secondaryContainer = DarkOceanTeal,
    onSecondaryContainer = Color(0xFFB5EDFF),
    tertiary = DarkEmeraldGreen,
    onTertiary = Color(0xFF7CF7D5),
    background = DarkIndigoBg,
    onBackground = Color(0xFFE2E1EC),
    surface = DarkLavenderSurface,
    onSurface = Color(0xFFE5E0F2),
    surfaceVariant = DarkOceanTeal,
    onSurfaceVariant = Color(0xFFC1ECF8)
)

@Composable
fun KoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
