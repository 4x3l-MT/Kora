package com.ax3lmt.kora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ax3lmt.kora.model.CyclePhase
import com.ax3lmt.kora.model.CyclePrediction
import com.ax3lmt.kora.model.DailyLog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    prediction: CyclePrediction,
    allLogs: List<DailyLog>,
    onDateSelected: (LocalDate) -> Unit
) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val logsMap = remember(allLogs) { allLogs.associateBy { it.date } }
    val selectedDayLog = logsMap[selectedDate]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Month Selector Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "Mes anterior")
            }

            val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-ES"))
            val monthTitle = currentYearMonth.format(monthFormatter).replaceFirstChar { it.uppercase() }

            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "Mes siguiente")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Weekday Legend
        Row(modifier = Modifier.fillMaxWidth()) {
            val daysOfWeek = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
            daysOfWeek.forEach { dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Days Grid
        val firstDayOfMonth = currentYearMonth.atDay(1)
        val firstDayOfWeekIndex = (firstDayOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
        val daysInMonth = currentYearMonth.lengthOfMonth()

        val totalCells = firstDayOfWeekIndex + daysInMonth

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(totalCells) { index ->
                if (index >= firstDayOfWeekIndex) {
                    val dayOfMonth = index - firstDayOfWeekIndex + 1
                    val date = currentYearMonth.atDay(dayOfMonth)
                    val log = logsMap[date]

                    val isConfirmedPeriod = log?.isPeriodDay == true
                    val isPredictedPeriod = isDateInPredictedPeriod(date, prediction)
                    val isOvulationDay = prediction.estimatedOvulationDate != null && date == prediction.estimatedOvulationDate
                    val phase = prediction.getPhaseForDate(date, isConfirmedPeriod)
                    val isToday = date == LocalDate.now()
                    val isSelected = date == selectedDate
                    val hasSymptoms = log?.symptoms?.isNotEmpty() == true

                    DayCell(
                        day = dayOfMonth,
                        isToday = isToday,
                        isSelected = isSelected,
                        isConfirmedPeriod = isConfirmedPeriod,
                        isPredictedPeriod = isPredictedPeriod,
                        isOvulationDay = isOvulationDay,
                        phase = phase,
                        hasSymptoms = hasSymptoms,
                        onClick = {
                            selectedDate = date
                        }
                    )
                } else {
                    Spacer(modifier = Modifier.aspectRatio(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Calendar Legend showing ALL 4 Cycle Phases & Ovulation Day
        CalendarLegend()

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Date Detail Card
        SelectedDateCard(
            date = selectedDate,
            prediction = prediction,
            log = selectedDayLog,
            onEditClick = { onDateSelected(selectedDate) }
        )
    }
}

private fun isDateInPredictedPeriod(date: LocalDate, prediction: CyclePrediction): Boolean {
    val start = prediction.nextPredictedPeriodStartDate ?: return false
    val end = prediction.nextPredictedPeriodEndDate ?: return false
    return !date.isBefore(start) && !date.isAfter(end)
}

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isConfirmedPeriod: Boolean,
    isPredictedPeriod: Boolean,
    isOvulationDay: Boolean,
    phase: CyclePhase,
    hasSymptoms: Boolean,
    onClick: () -> Unit
) {
    val (cellBg, textColor) = when {
        isConfirmedPeriod -> Pair(Color(0xFFD81B60), Color.White)
        isPredictedPeriod -> Pair(Color(0xFFFFCDD2), Color(0xFF880E4F))
        isOvulationDay -> Pair(Color(0xFF7B1FA2), Color.White)
        phase == CyclePhase.OVULATORY -> Pair(Color(0xFFE1BEE7), Color(0xFF4A148C))
        phase == CyclePhase.FOLLICULAR -> Pair(Color(0xFFB2DFDB), Color(0xFF004D40))
        phase == CyclePhase.LUTEAL -> Pair(Color(0xFFFFECB3), Color(0xFF5D4037))
        else -> Pair(Color.Transparent, MaterialTheme.colorScheme.onSurface)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(cellBg)
            .then(
                if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else if (isToday) Modifier.border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$day",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            if (isOvulationDay) {
                Text(
                    text = "★",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F)
                )
            } else if (hasSymptoms) {
                Spacer(modifier = Modifier.height(1.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isConfirmedPeriod) Color.White else Color(0xFFE65100))
                )
            }
        }
    }
}

@Composable
private fun CalendarLegend() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = Color(0xFFD81B60), label = "Menstrual")
            LegendItem(color = Color(0xFF00897B), label = "Folicular")
            LegendItem(color = Color(0xFF8E24AA), label = "Ventana Fértil")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = Color(0xFF7B1FA2), label = "Ovulación ★")
            LegendItem(color = Color(0xFFD84315), label = "Lútea")
            LegendItem(color = Color(0xFFE65100), label = "Síntoma")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SelectedDateCard(
    date: LocalDate,
    prediction: CyclePrediction,
    log: DailyLog?,
    onEditClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM yyyy", Locale.forLanguageTag("es-ES"))
    val dateStr = date.format(formatter).replaceFirstChar { it.uppercase() }

    val isOvulationDay = prediction.estimatedOvulationDate != null && date == prediction.estimatedOvulationDate
    val phase = prediction.getPhaseForDate(date, log?.isPeriodDay == true)
    val (phaseBg, phaseTextColor) = when {
        isOvulationDay -> Pair(Color(0xFF7B1FA2), Color.White)
        phase == CyclePhase.MENSTRUAL -> Pair(Color(0xFFFCE4EC), Color(0xFFD81B60))
        phase == CyclePhase.FOLLICULAR -> Pair(Color(0xFFE0F2F1), Color(0xFF00695C))
        phase == CyclePhase.OVULATORY -> Pair(Color(0xFFF3E5F5), Color(0xFF7B1FA2))
        phase == CyclePhase.LUTEAL -> Pair(Color(0xFFFFF8E1), Color(0xFFE65100))
        else -> Pair(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = phaseBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isOvulationDay) "★ Día Estimado de Ovulación (Ventana Fértil)" else phase.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = phaseTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Button(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (log != null && (log.isPeriodDay || log.symptoms.isNotEmpty())) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (log != null && (log.isPeriodDay || log.symptoms.isNotEmpty())) "Editar" else "Anotar")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = phase.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (log?.isPeriodDay == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sangrado: ${log.flowLevel.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD81B60)
                )
            }

            if (log?.symptoms?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Síntomas: ${log.symptoms.joinToString { it.nameEs }}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (!log?.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Notas: \"${log.notes}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
