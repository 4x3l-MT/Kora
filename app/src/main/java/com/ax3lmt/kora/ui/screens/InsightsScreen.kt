package com.ax3lmt.kora.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ax3lmt.kora.model.CyclePrediction
import com.ax3lmt.kora.model.DailyLog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun InsightsScreen(
    prediction: CyclePrediction,
    allLogs: List<DailyLog>
) {
    val periodDays = allLogs.filter { it.isPeriodDay }.map { it.date }.sorted()

    // Group periods to display history
    val cyclesList = rememberCycleHistory(periodDays)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Estadísticas & Análisis Real",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Datos calculados exclusivamente con base en tu historial registrado.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tus Métricas Personalizadas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = "Promedio de Ciclo",
                            value = "${prediction.averageCycleLengthDays} días"
                        )
                        MetricItem(
                            label = "Promedio Período",
                            value = "${prediction.averagePeriodLengthDays} días"
                        )
                        MetricItem(
                            label = "Ciclos Anotados",
                            value = "${prediction.totalCyclesRecorded}"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (prediction.isBasedOnCustomHistory)
                                    "Predicciones activas 100% basadas en tu promedio real de ciclo."
                                else
                                    "Se utiliza el valor inicial por falta de suficientes ciclos registrados.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Historial de Períodos Anteriores",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (cyclesList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aún no hay ciclos completos registrados.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Comienza a anotar tus días de sangrado en el calendario para generar tu historial.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(cyclesList) { item ->
                PastCycleCard(cycleItem = item)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

data class CycleHistoryItem(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val durationDays: Int,
    val cycleLengthToNext: Int?
)

private fun rememberCycleHistory(periodDays: List<LocalDate>): List<CycleHistoryItem> {
    if (periodDays.isEmpty()) return emptyList()

    val periodStarts = mutableListOf<LocalDate>()
    val periodEnds = mutableListOf<LocalDate>()

    var currentStart: LocalDate? = null
    var currentPrev: LocalDate? = null

    for (day in periodDays) {
        if (currentStart == null) {
            currentStart = day
            currentPrev = day
        } else {
            if (ChronoUnit.DAYS.between(currentPrev, day) == 1L) {
                currentPrev = day
            } else {
                periodStarts.add(currentStart)
                periodEnds.add(currentPrev!!)
                currentStart = day
                currentPrev = day
            }
        }
    }
    if (currentStart != null && currentPrev != null) {
        periodStarts.add(currentStart)
        periodEnds.add(currentPrev)
    }

    val result = mutableListOf<CycleHistoryItem>()
    for (i in periodStarts.indices) {
        val start = periodStarts[i]
        val end = periodEnds[i]
        val duration = (ChronoUnit.DAYS.between(start, end) + 1).toInt()
        val cycleLength = if (i < periodStarts.size - 1) {
            ChronoUnit.DAYS.between(periodStarts[i], periodStarts[i + 1]).toInt()
        } else {
            null
        }
        result.add(
            CycleHistoryItem(
                startDate = start,
                endDate = end,
                durationDays = duration,
                cycleLengthToNext = cycleLength
            )
        )
    }
    return result.reversed() // Most recent first
}

@Composable
private fun PastCycleCard(cycleItem: CycleHistoryItem) {
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es-ES"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Inicio: ${cycleItem.startDate.format(formatter)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Fin: ${cycleItem.endDate.format(formatter)} (${cycleItem.durationDays} días de sangrado)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = Color(0xFFE91E63).copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (cycleItem.cycleLengthToNext != null)
                        "Ciclo: ${cycleItem.cycleLengthToNext} días"
                    else
                        "Ciclo Actual",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE91E63),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
