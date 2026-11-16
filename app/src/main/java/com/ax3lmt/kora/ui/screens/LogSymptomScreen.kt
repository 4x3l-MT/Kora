package com.ax3lmt.kora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ax3lmt.kora.model.DailyLog
import com.ax3lmt.kora.model.FlowLevel
import com.ax3lmt.kora.model.Symptom
import com.ax3lmt.kora.model.SymptomCategory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogSymptomScreen(
    initialDate: LocalDate,
    currentLog: DailyLog?,
    onSaveLog: (date: LocalDate, isPeriod: Boolean, flow: FlowLevel, symptoms: Set<Symptom>, pain: Int, notes: String) -> Unit,
    onDeleteLog: (LocalDate) -> Unit,
    onBackClick: () -> Unit
) {
    var currentDate by remember(initialDate) { mutableStateOf(initialDate) }

    var isPeriodDay by remember(currentLog) { mutableStateOf(currentLog?.isPeriodDay == true) }
    var flowLevel by remember(currentLog) { mutableStateOf(currentLog?.flowLevel ?: FlowLevel.MEDIUM) }
    var selectedSymptoms by remember(currentLog) { mutableStateOf(currentLog?.symptoms ?: emptySet()) }
    var painIntensity by remember(currentLog) { mutableFloatStateOf((currentLog?.painIntensity ?: 0).toFloat()) }
    var notes by remember(currentLog) { mutableStateOf(currentLog?.notes ?: "") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = "Registrar Día & Síntomas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Date Selector Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentDate = currentDate.minusDays(1) }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "Día anterior")
                }

                val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-ES"))
                val formattedDate = currentDate.format(formatter).replaceFirstChar { it.uppercase() }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (currentDate == LocalDate.now()) {
                        Text(
                            text = "(Hoy)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(onClick = { currentDate = currentDate.plusDays(1) }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "Día siguiente")
                }
            }
        }

        // Period Toggle & Flow Level
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F5))
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
                            text = "Menstruación / Sangrado hoy",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE91E63)
                        )
                        Text(
                            text = "Marca si hoy tuviste tu periodo menstrual",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }

                    Switch(
                        checked = isPeriodDay,
                        onCheckedChange = { isPeriodDay = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFE91E63)
                        )
                    )
                }

                if (isPeriodDay) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Intensidad del flujo:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FlowLevel.entries.filter { it != FlowLevel.NONE }.forEach { level ->
                            FilterChip(
                                selected = flowLevel == level,
                                onClick = { flowLevel = level },
                                label = { Text(level.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE91E63),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Pain Slider
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Nivel de Cólicos / Dolor (0 a 5)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${painIntensity.toInt()}/5",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (painIntensity >= 4) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = painIntensity,
                    onValueChange = { painIntensity = it },
                    valueRange = 0f..5f,
                    steps = 4,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                val painDescription = when (painIntensity.toInt()) {
                    0 -> "Sin dolor"
                    1 -> "Dolor leve perceptible"
                    2 -> "Molestia moderada"
                    3 -> "Cólicos incómodos"
                    4 -> "Dolor fuerte"
                    5 -> "Dolor muy intenso / Incapacitante"
                    else -> ""
                }

                Text(
                    text = painDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Symptoms Category Selector
        Text(
            text = "Selecciona Síntomas o Situaciones del Día",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        SymptomCategory.entries.forEach { category ->
            SymptomCategoryCard(
                category = category,
                selectedSymptoms = selectedSymptoms,
                onSymptomToggle = { symptom ->
                    selectedSymptoms = if (selectedSymptoms.contains(symptom)) {
                        selectedSymptoms - symptom
                    } else {
                        selectedSymptoms + symptom
                    }
                }
            )
        }

        // Notes Field
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notas adicionales o diario personal") },
            placeholder = { Text("Ej: Tomé té de manzanilla, dormí 8h, molestia al hacer ejercicio...") },
            minLines = 3,
            maxLines = 6,
            shape = RoundedCornerShape(12.dp)
        )

        // Action Buttons
        Button(
            onClick = {
                onSaveLog(
                    currentDate,
                    isPeriodDay,
                    if (isPeriodDay) flowLevel else FlowLevel.NONE,
                    selectedSymptoms,
                    painIntensity.toInt(),
                    notes
                )
                onBackClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardar Registro")
        }

        if (currentLog != null && (currentLog.isPeriodDay || currentLog.symptoms.isNotEmpty() || currentLog.notes.isNotBlank())) {
            OutlinedButton(
                onClick = {
                    onDeleteLog(currentDate)
                    onBackClick()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Eliminar Registro de este Día")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SymptomCategoryCard(
    category: SymptomCategory,
    selectedSymptoms: Set<Symptom>,
    onSymptomToggle: (Symptom) -> Unit
) {
    val symptoms = Symptom.entries.filter { it.category == category }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                symptoms.forEach { symptom ->
                    val isSelected = selectedSymptoms.contains(symptom)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSymptomToggle(symptom) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (symptom.isRedFlag) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Atención requerida",
                                        tint = if (isSelected) Color.White else Color(0xFFD32F2F),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(symptom.nameEs)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (symptom.isRedFlag) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}
