package com.ax3lmt.kora.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.material.icons.filled.Favorite

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Calendar : Screen("calendar", "Calendario", Icons.Default.CalendarMonth)
    object LogSymptom : Screen("log_symptom?date={date}", "Registrar Síntomas", Icons.Default.EditNote) {
        fun createRoute(dateStr: String? = null): String {
            return if (dateStr != null) "log_symptom?date=$dateStr" else "log_symptom?date="
        }
    }
    object Recommendations : Screen("recommendations", "Recomendaciones", Icons.Default.Favorite)
    object Insights : Screen("insights", "Estadísticas", Icons.Default.Analytics)
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
}
