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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ax3lmt.kora.model.CyclePhase
import com.ax3lmt.kora.model.CyclePrediction
import com.ax3lmt.kora.model.Recommendation
import com.ax3lmt.kora.model.RecommendationPriority
import java.time.LocalDate

@Composable
fun RecommendationsScreen(
    prediction: CyclePrediction,
    activeRecommendations: List<Recommendation>,
    catalogRecommendations: List<Recommendation>
) {
    val today = LocalDate.now()
    val currentPhase = prediction.getPhaseForDate(today)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Recomendaciones & Cuidados",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Catálogo completo de alivio por síntoma y análisis biométrico de precisión.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Biometric & Anomaly Analysis Card
        item {
            BiometricAnalysisCard(prediction = prediction)
        }

        // Today's Phase Overview Banner
        item {
            TodayPhaseBanner(currentPhase = currentPhase)
        }

        // Active 20-Day Symptom Recommendations (If any extraordinary symptoms logged)
        val realActiveRecs = activeRecommendations.filter { it.id != "rec_general_wellness" }
        if (realActiveRecs.isNotEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Alertas de tus Últimos 20 Días",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                }
            }

            items(realActiveRecs) { rec ->
                RecommendationItemCard(recommendation = rec)
            }
        }

        // Section Title: Accessible Full Catalog of Symptom Relief
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Catálogo de Cuidados y Alivio por Síntoma",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Consulta estas guías de cuidado específico en cualquier momento desde el menú.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(catalogRecommendations) { rec ->
            RecommendationItemCard(recommendation = rec)
        }

        // Phase Guides
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Guía de Autocuidado según tus 4 Fases Hormonales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            PhaseGuideCard(
                phaseTitle = "1. Fase Menstrual (Sangrado)",
                badgeColor = Color(0xFFD81B60),
                nutritionAdvice = "Alimentos ricos en hierro (espinacas, lentejas, carne magra) + vitamina C (cítricos) e infusiones calientes.",
                exerciseAdvice = "Caminatas suaves, estiramientos y yoga restaurativo. Prioriza el descanso si hay cólicos.",
                mentalAdvice = "Compresas tibias en abdomen bajo, desconexión y sueño reparador."
            )
        }

        item {
            PhaseGuideCard(
                phaseTitle = "2. Fase Folicular (Pre-ovulación)",
                badgeColor = Color(0xFF00695C),
                nutritionAdvice = "Proteínas magras, vegetales verdes, alimentos fermentados (kefir/yogur) y semillas de sésamo.",
                exerciseAdvice = "Pico ascendente de energía. Momento ideal para fuerza, entrenamientos de alta intensidad (HIIT) o nuevos deportes.",
                mentalAdvice = "Alta claridad mental y creatividad. Excelente momento para iniciar proyectos."
            )
        }

        item {
            PhaseGuideCard(
                phaseTitle = "3. Ventana Fértil / Ovulación",
                badgeColor = Color(0xFF7B1FA2),
                nutritionAdvice = "Frutos rojos, aguacate, frutos secos y alimentos ricos en fibra y antioxidantes.",
                exerciseAdvice = "Máxima energía y resistencia física. Entrenamientos intensos de cardio y fuerza.",
                mentalAdvice = "Pico de sociabilidad, comunicación fluida y confianza personal."
            )
        }

        item {
            PhaseGuideCard(
                phaseTitle = "4. Fase Lútea (Post-ovulación)",
                badgeColor = Color(0xFFE65100),
                nutritionAdvice = "Carbohidratos complejos (camote, avena) y alimentos ricos en magnesio (plátano, cacao puro) para reducir antojos.",
                exerciseAdvice = "Pilates, caminatas a ritmo moderado o natación suave conforme disminuye la energía.",
                mentalAdvice = "Reducir cafeína para evitar ansiedad/irritabilidad. Practicar respiración consciente."
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BiometricAnalysisCard(prediction: CyclePrediction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Análisis Biométrico Adaptativo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Media Ponderada (L_ciclo)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${prediction.weightedCycleLengthDays} días",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Column {
                    Text(
                        text = "Fase Lútea (L_lútea)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${prediction.customLutealLengthDays} días",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Column {
                    Text(
                        text = "Variación (σ)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "±${"%.1f".format(prediction.cycleVariationDays)}d",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (prediction.hasAnomaly) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Anomalía/Irregularidad detectada (variación >4d). La ventana fértil se ha ampliado con un intervalo de confianza adaptativo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB71C1C),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Nivel de confianza: ${prediction.confidenceLevel}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayPhaseBanner(currentPhase: CyclePhase) {
    val (bgColor, textColor) = when (currentPhase) {
        CyclePhase.MENSTRUAL -> Pair(Color(0xFFFCE4EC), Color(0xFFD81B60))
        CyclePhase.FOLLICULAR -> Pair(Color(0xFFE0F2F1), Color(0xFF00695C))
        CyclePhase.OVULATORY -> Pair(Color(0xFFF3E5F5), Color(0xFF7B1FA2))
        CyclePhase.LUTEAL -> Pair(Color(0xFFFFF8E1), Color(0xFFE65100))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Tu Fase Actual Hoy:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = 0.8f)
            )
            Text(
                text = currentPhase.displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = currentPhase.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF202020)
            )
        }
    }
}

@Composable
private fun RecommendationItemCard(recommendation: Recommendation) {
    val (bgColor, borderColor, icon) = when (recommendation.priority) {
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
                    text = "Acción recomendada: ${recommendation.actionText}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = borderColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun PhaseGuideCard(
    phaseTitle: String,
    badgeColor: Color,
    nutritionAdvice: String,
    exerciseAdvice: String,
    mentalAdvice: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = phaseTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )

            GuideRow(icon = Icons.Default.Restaurant, label = "Nutrición", text = nutritionAdvice)
            GuideRow(icon = Icons.Default.FitnessCenter, label = "Ejercicio", text = exerciseAdvice)
            GuideRow(icon = Icons.Default.SelfImprovement, label = "Bienestar", text = mentalAdvice)
        }
    }
}

@Composable
private fun GuideRow(icon: ImageVector, label: String, text: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
