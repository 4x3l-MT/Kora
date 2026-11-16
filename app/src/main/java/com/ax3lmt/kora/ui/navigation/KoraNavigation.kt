package com.ax3lmt.kora.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ax3lmt.kora.ui.screens.CalendarScreen
import com.ax3lmt.kora.ui.screens.HomeScreen
import com.ax3lmt.kora.ui.screens.InsightsScreen
import com.ax3lmt.kora.ui.screens.LogSymptomScreen
import com.ax3lmt.kora.ui.screens.OnboardingDialog
import com.ax3lmt.kora.ui.screens.RecommendationsScreen
import com.ax3lmt.kora.ui.screens.SettingsScreen
import com.ax3lmt.kora.viewmodel.KoraViewModel
import java.time.LocalDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KoraNavigation(
    viewModel: KoraViewModel,
    navController: NavHostController = rememberNavController()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isOnboardingCompleted by viewModel.isOnboardingCompletedState.collectAsState()
    val prediction by viewModel.predictionState.collectAsState()
    val recommendations by viewModel.recommendationsState.collectAsState()
    val allLogs by viewModel.allLogsState.collectAsState()
    val selectedDate by viewModel.selectedDateState.collectAsState()
    val selectedLog by viewModel.selectedLogState.collectAsState()
    val defaultCycle by viewModel.defaultCycleDaysState.collectAsState()
    val defaultPeriod by viewModel.defaultPeriodDaysState.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    if (!isOnboardingCompleted) {
        OnboardingDialog(
            onCompleteSetup = { periodDays, cycleDays ->
                viewModel.completeInitialOnboarding(periodDays, cycleDays)
            }
        )
    }

    val items = listOf(
        Screen.Home,
        Screen.Calendar,
        Screen.LogSymptom,
        Screen.Recommendations,
        Screen.Insights,
        Screen.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Menstruación & Salud",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp)
                )
                Text(
                    text = "100% Privado en tu Dispositivo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 28.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                items.forEach { screen ->
                    val isSelected = currentRoute.startsWith(screen.route.split("?")[0])
                    NavigationDrawerItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when {
                                currentRoute.startsWith("home") -> "Kora - Menstrual"
                                currentRoute.startsWith("calendar") -> "Calendario Menstrual"
                                currentRoute.startsWith("log_symptom") -> "Registrar Síntomas"
                                currentRoute.startsWith("recommendations") -> "Recomendaciones & Cuidados"
                                currentRoute.startsWith("insights") -> "Estadísticas del Ciclo"
                                currentRoute.startsWith("settings") -> "Ajustes & Privacidad"
                                else -> "Kora"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Abrir Menú")
                        }
                    },
                    actions = {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    items.forEach { screen ->
                        val isSelected = currentRoute.startsWith(screen.route.split("?")[0])
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    composable(Screen.Home.route) {
                        val todayLog = allLogs.find { it.date == LocalDate.now() }
                        HomeScreen(
                            prediction = prediction,
                            recommendations = recommendations,
                            todayLog = todayLog,
                            onNavigateToLog = { date ->
                                viewModel.selectDate(date)
                                navController.navigate(Screen.LogSymptom.createRoute(date.toString()))
                            },
                            onNavigateToCalendar = {
                                navController.navigate(Screen.Calendar.route)
                            }
                        )
                    }

                    composable(Screen.Calendar.route) {
                        CalendarScreen(
                            prediction = prediction,
                            allLogs = allLogs,
                            onDateSelected = { date ->
                                viewModel.selectDate(date)
                                navController.navigate(Screen.LogSymptom.createRoute(date.toString()))
                            }
                        )
                    }

                    composable(Screen.LogSymptom.route) { backStackEntry ->
                        val dateStr = backStackEntry.arguments?.getString("date")
                        val initialDate = try {
                            if (!dateStr.isNullOrBlank()) LocalDate.parse(dateStr) else selectedDate
                        } catch (_: Exception) {
                            selectedDate
                        }

                        LaunchedEffect(initialDate) {
                            viewModel.selectDate(initialDate)
                        }

                        LogSymptomScreen(
                            initialDate = initialDate,
                            currentLog = selectedLog,
                            onSaveLog = { date, isPeriod, flow, symptoms, pain, notes ->
                                viewModel.saveLog(date, isPeriod, flow, symptoms, pain, notes)
                            },
                            onDeleteLog = { date ->
                                viewModel.deleteLog(date)
                            },
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(Screen.Recommendations.route) {
                        val catalogRecommendations by viewModel.catalogRecommendationsState.collectAsState()
                        RecommendationsScreen(
                            prediction = prediction,
                            activeRecommendations = recommendations,
                            catalogRecommendations = catalogRecommendations
                        )
                    }

                    composable(Screen.Insights.route) {
                        InsightsScreen(
                            prediction = prediction,
                            allLogs = allLogs
                        )
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            currentDefaultCycle = defaultCycle,
                            currentDefaultPeriod = defaultPeriod,
                            onSaveSettings = { cycleDays, periodDays ->
                                viewModel.updateSettings(cycleDays, periodDays)
                            },
                            onClearAllData = {
                                viewModel.clearAllData()
                            }
                        )
                    }
                }
            }
        }
    }
}
