package com.ax3lmt.kora.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.ui.res.painterResource
import com.ax3lmt.kora.R
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ax3lmt.kora.model.CyclePrediction
import com.ax3lmt.kora.model.DailyLog
import com.ax3lmt.kora.model.Recommendation
import com.ax3lmt.kora.model.RecommendationPriority
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

@Composable
fun HomeScreen(
    prediction: CyclePrediction,
    recommendations: List<Recommendation>,
    todayLog: DailyLog?,
    onNavigateToLog: (LocalDate) -> Unit,
    onNavigateToCalendar: () -> Unit
) {
    val today = LocalDate.now()
    val isPeriodToday = todayLog?.isPeriodDay == true

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            HeaderSection(today = today)
        }

        item {
            CycleStatusCard(
                prediction = prediction,
                isPeriodToday = isPeriodToday,
                today = today
            )
        }

        item {
            TodayQuickLogCard(
                todayLog = todayLog,
                onLogClick = { onNavigateToLog(today) }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recomendaciones (Últimos 20 días)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Basado únicamente en tus notas y síntomas registrados en tu dispositivo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(recommendations) { recommendation ->
            RecommendationCard(recommendation = recommendation)
        }

        item {
            OutlinedButton(
                onClick = onNavigateToCalendar,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver Calendario Completo")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeaderSection(today: LocalDate) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-ES"))
    val formattedDate = today.format(formatter).replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Tu Salud & Ciclo Local",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "Logo Kora",
            modifier = Modifier.size(54.dp)
        )
    }
}

@Composable
private fun CycleStatusCard(
    prediction: CyclePrediction,
    isPeriodToday: Boolean,
    today: LocalDate
) {
    val roseBg = Color(0xFFFFF0F5)
    val roseAccent = Color(0xFFE91E63)
    val purpleAccent = Color(0xFF9C27B0)

    val nextPeriodStart = prediction.nextPredictedPeriodStartDate
    val daysUntilNext = if (nextPeriodStart != null) ChronoUnit.DAYS.between(today, nextPeriodStart) else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = roseBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = if (isPeriodToday) roseAccent else purpleAccent,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isPeriodToday) "En Período Menstrual" else if (prediction.isBasedOnCustomHistory) "Predicción Basada en tus Datos" else "Predicción Inicial",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isPeriodToday) {
                        Image(
                            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Menstruación",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = roseAccent
                        )
                    } else if (daysUntilNext != null) {
                        if (daysUntilNext > 0) {
                            Text(
                                text = "$daysUntilNext",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = roseAccent
                            )
                            Text(
                                text = if (daysUntilNext == 1L) "Día restante" else "Días faltantes",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF49454F)
                            )
                        } else if (daysUntilNext == 0L) {
                            Text(
                                text = "Hoy",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = roseAccent
                            )
                            Text(
                                text = "Próximo período",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF49454F)
                            )
                        } else {
                            Text(
                                text = "Atraso",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB71C1C)
                            )
                            Text(
                                text = "${abs(daysUntilNext)} días",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF49454F)
                            )
                        }
                    } else {
                        Text(
                            text = "--",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF49454F)
                        )
                        Text(
                            text = "Registra datos",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF49454F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Prom. Ciclo", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                    Text(
                        text = "${prediction.averageCycleLengthDays} días",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Prom. Período", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                    Text(
                        text = "${prediction.averagePeriodLengthDays} días",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Historial", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                    Text(
                        text = "${prediction.totalCyclesRecorded} ciclos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                }
            }

            if (!prediction.isBasedOnCustomHistory) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "💡 Registra 2 o más períodos para personalizar 100% las predicciones a tu cuerpo.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF303030),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TodayQuickLogCard(
    todayLog: DailyLog?,
    onLogClick: () -> Unit
) {
    val hasLoggedToday = todayLog != null && (todayLog.isPeriodDay || todayLog.symptoms.isNotEmpty() || todayLog.notes.isNotBlank())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (hasLoggedToday) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasLoggedToday) Icons.Default.CheckCircle else Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasLoggedToday) "Registro de hoy completado" else "Anota tus síntomas de hoy",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (hasLoggedToday) {
                        val symptomsCount = todayLog.symptoms.size
                        "${if (todayLog.isPeriodDay) "Día de sangrado. " else ""}$symptomsCount síntoma(s) anotado(s)."
                    } else {
                        "Mantén al día tus cólicos, humor, flujo o molestias atípicas."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onLogClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasLoggedToday) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (hasLoggedToday) "Editar" else "Anotar")
            }
        }
    }
}

@Composable
private fun RecommendationCard(recommendation: Recommendation) {
    val tripleInfo: Triple<Color, Color, ImageVector> = when (recommendation.priority) {
        RecommendationPriority.MEDICAL_ALERT -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFD32F2F),
            Icons.Default.MedicalServices
        )
        RecommendationPriority.RECOMMENDED -> Triple(
            Color(0xFFFFF8E1),
            Color(0xFFFFA000),
            Icons.Default.Info
        )
        RecommendationPriority.WELLNESS_TIP -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF388E3C),
            Icons.Default.CheckCircle
        )
    }

    val bgColor = tripleInfo.first
    val borderColor = tripleInfo.second
    val icon = tripleInfo.third

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = borderColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = borderColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Acción: ${recommendation.actionText}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = borderColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
