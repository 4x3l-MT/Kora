package com.ax3lmt.kora.data.repository

import android.content.Context
import com.ax3lmt.kora.data.db.KoraDbHelper
import com.ax3lmt.kora.model.CyclePrediction
import com.ax3lmt.kora.model.DailyLog
import com.ax3lmt.kora.model.FlowLevel
import com.ax3lmt.kora.model.Recommendation
import com.ax3lmt.kora.model.RecommendationPriority
import com.ax3lmt.kora.model.Symptom
import com.ax3lmt.kora.model.SymptomCategory
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class MenstrualRepository(context: Context) {

    private val dbHelper = KoraDbHelper(context)

    fun isOnboardingCompleted(): Boolean {
        return dbHelper.getSetting("onboarding_completed", "false").toBoolean()
    }

    fun saveInitialSetup(periodDays: Int?, cycleDays: Int?) {
        if (periodDays != null) {
            dbHelper.saveSetting("default_period_length", periodDays.toString())
            dbHelper.saveSetting("period_unknown", "false")
        } else {
            dbHelper.saveSetting("default_period_length", "5")
            dbHelper.saveSetting("period_unknown", "true")
        }

        if (cycleDays != null) {
            dbHelper.saveSetting("default_cycle_length", cycleDays.toString())
            dbHelper.saveSetting("cycle_unknown", "false")
        } else {
            dbHelper.saveSetting("default_cycle_length", "28")
            dbHelper.saveSetting("cycle_unknown", "true")
        }

        dbHelper.saveSetting("onboarding_completed", "true")
    }

    fun isCycleUnknown(): Boolean {
        return dbHelper.getSetting("cycle_unknown", "false").toBoolean()
    }

    fun isPeriodUnknown(): Boolean {
        return dbHelper.getSetting("period_unknown", "false").toBoolean()
    }

    fun saveDefaultCycleSetting(days: Int) {
        dbHelper.saveSetting("default_cycle_length", days.toString())
        dbHelper.saveSetting("cycle_unknown", "false")
    }

    fun getDefaultCycleSetting(): Int {
        return dbHelper.getSetting("default_cycle_length", "28").toIntOrNull() ?: 28
    }

    fun saveDefaultPeriodSetting(days: Int) {
        dbHelper.saveSetting("default_period_length", days.toString())
        dbHelper.saveSetting("period_unknown", "false")
    }

    fun getDefaultPeriodSetting(): Int {
        return dbHelper.getSetting("default_period_length", "5").toIntOrNull() ?: 5
    }

    fun saveLog(log: DailyLog) {
        val previousLog = dbHelper.getDailyLog(log.date.minusDays(1))
        val isFirstDayOfPeriod = log.isPeriodDay && (previousLog == null || !previousLog.isPeriodDay)

        dbHelper.saveDailyLog(log)

        // If registering the FIRST day of bleeding, auto-fill the next (N - 1) days based on period duration
        if (isFirstDayOfPeriod) {
            val periodDuration = getDefaultPeriodSetting().coerceIn(2, 10)
            for (i in 1 until periodDuration) {
                val futureDate = log.date.plusDays(i.toLong())
                val existingLog = dbHelper.getDailyLog(futureDate)
                if (existingLog == null || !existingLog.isPeriodDay) {
                    val autoFillLog = (existingLog ?: DailyLog(date = futureDate)).copy(
                        isPeriodDay = true,
                        flowLevel = if (i == 1 || i == 2) FlowLevel.MEDIUM else FlowLevel.LIGHT
                    )
                    dbHelper.saveDailyLog(autoFillLog)
                }
            }
        }
    }

    fun deleteLog(date: LocalDate) {
        dbHelper.deleteDailyLog(date)
    }

    fun getLog(date: LocalDate): DailyLog? {
        return dbHelper.getDailyLog(date)
    }

    fun getAllLogs(): List<DailyLog> {
        return dbHelper.getAllLogs()
    }

    fun getLogsForRange(startDate: LocalDate, endDate: LocalDate): List<DailyLog> {
        return dbHelper.getLogsForRange(startDate, endDate)
    }

    fun clearAllData() {
        dbHelper.clearAllData()
    }

    /**
     * Dynamically calculates cycle predictions using weighted cycle lengths (L_cycle),
     * biomarker-adjusted luteal phase (L_luteal), standard deviation anomaly detection,
     * and adaptive fertile window confidence intervals.
     */
    fun calculatePrediction(today: LocalDate = LocalDate.now()): CyclePrediction {
        val logs = dbHelper.getAllLogs().sortedBy { it.date }
        val periodDays = logs.filter { it.isPeriodDay }.map { it.date }.sorted()

        if (periodDays.isEmpty()) {
            val defaultCycle = getDefaultCycleSetting()
            val defaultPeriod = getDefaultPeriodSetting()
            return CyclePrediction(
                averageCycleLengthDays = defaultCycle,
                weightedCycleLengthDays = defaultCycle,
                averagePeriodLengthDays = defaultPeriod,
                customLutealLengthDays = 14,
                cycleVariationDays = 0.0,
                hasAnomaly = false,
                confidenceLevel = "Inicial (Registra datos para adaptar)",
                confidenceWindowMarginDays = 0,
                totalCyclesRecorded = 0,
                lastPeriodStartDate = null,
                nextPredictedPeriodStartDate = null,
                nextPredictedPeriodEndDate = null,
                estimatedOvulationDate = null,
                fertileWindowStart = null,
                fertileWindowEnd = null,
                isBasedOnCustomHistory = false
            )
        }

        // Group consecutive period days into periods
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
                    // Gap found -> finish previous period
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

        val periodDurations = periodStarts.zip(periodEnds).map { (start, end) ->
            (ChronoUnit.DAYS.between(start, end) + 1).toInt()
        }

        val cycleLengths = mutableListOf<Int>()
        for (i in 0 until periodStarts.size - 1) {
            val length = ChronoUnit.DAYS.between(periodStarts[i], periodStarts[i + 1]).toInt()
            if (length in 15..60) {
                cycleLengths.add(length)
            }
        }

        val hasCustomHistory = cycleLengths.isNotEmpty()

        // 1. Simple average cycle length
        val avgCycleLength = if (hasCustomHistory) {
            cycleLengths.average().toInt().coerceIn(18, 50)
        } else {
            getDefaultCycleSetting()
        }

        // 2. Weighted average cycle length (higher weights to more recent cycles: w_i = i)
        val weightedCycleLength = if (hasCustomHistory) {
            var weightedSum = 0
            var weightSum = 0
            cycleLengths.forEachIndexed { index, length ->
                val weight = index + 1
                weightedSum += weight * length
                weightSum += weight
            }
            if (weightSum > 0) (weightedSum.toDouble() / weightSum).toInt().coerceIn(18, 50) else avgCycleLength
        } else {
            getDefaultCycleSetting()
        }

        // 3. Average period duration
        val avgPeriodLength = if (periodDurations.isNotEmpty()) {
            periodDurations.average().toInt().coerceIn(2, 12)
        } else {
            getDefaultPeriodSetting()
        }

        // 4. Luteal Phase customization based on Ovulation Biomarkers logged (LH surge, BBT rise, Ovulation pain, Mucus)
        val biomarkerSymptoms = setOf(
            Symptom.POSITIVE_LH_TEST,
            Symptom.BASAL_TEMP_RISE,
            Symptom.OVULATION_PAIN,
            Symptom.CERVICAL_MUCUS_EGG_WHITE
        )

        val detectedLutealLengths = mutableListOf<Int>()
        for (i in 0 until periodStarts.size - 1) {
            val pStart = periodStarts[i]
            val pNextStart = periodStarts[i + 1]
            val bioLogsInCycle = logs.filter { l ->
                l.date.isAfter(pStart) && l.date.isBefore(pNextStart) && l.symptoms.any { it in biomarkerSymptoms }
            }
            val bioDate = bioLogsInCycle.minOfOrNull { it.date }
            if (bioDate != null) {
                val measuredLuteal = ChronoUnit.DAYS.between(bioDate, pNextStart).toInt()
                if (measuredLuteal in 10..16) {
                    detectedLutealLengths.add(measuredLuteal)
                }
            }
        }

        val customLutealLength = if (detectedLutealLengths.isNotEmpty()) {
            detectedLutealLengths.average().toInt().coerceIn(11, 14)
        } else {
            14 // Clinical default
        }

        // 5. Anomaly Detection & Standard Deviation (sigma)
        val sigma = if (cycleLengths.size >= 2) {
            val mean = cycleLengths.average()
            val sumSq = cycleLengths.sumOf { (it - mean) * (it - mean) }
            Math.sqrt(sumSq / cycleLengths.size)
        } else {
            0.0
        }

        val hasAnomaly = sigma > 4.0 || cycleLengths.any { Math.abs(it - avgCycleLength) > 5 }

        val (confidenceLevel, marginDays) = when {
            !hasCustomHistory -> Pair("Adaptativa Inicial", 0)
            sigma <= 1.8 -> Pair("Alta Precisión", 0)
            sigma <= 4.0 -> Pair("Moderada Adaptativa", 1)
            else -> Pair("Incertidumbre (Anomalía / Variación >4d)", Math.ceil(sigma).toInt().coerceIn(2, 4))
        }

        // 6. Projections using weighted cycle length and custom luteal phase
        val lastPeriodStart = periodStarts.lastOrNull()
        var nextPeriodStart: LocalDate? = null
        var nextPeriodEnd: LocalDate? = null
        var estimatedOvulation: LocalDate? = null
        var fertileStart: LocalDate? = null
        var fertileEnd: LocalDate? = null

        val activeCycleLength = if (hasCustomHistory) weightedCycleLength else avgCycleLength

        if (lastPeriodStart != null) {
            var candidate = lastPeriodStart.plusDays(activeCycleLength.toLong())
            while (candidate.isBefore(today.minusDays(avgPeriodLength.toLong()))) {
                candidate = candidate.plusDays(activeCycleLength.toLong())
            }
            nextPeriodStart = candidate
            nextPeriodEnd = nextPeriodStart.plusDays((avgPeriodLength - 1).coerceAtLeast(1).toLong())

            // Ovulation day D_ov = NextPeriodStart - L_luteal
            estimatedOvulation = nextPeriodStart.minusDays(customLutealLength.toLong())

            // Adaptive fertile window interval based on confidence & standard deviation
            fertileStart = estimatedOvulation.minusDays((4 + marginDays).toLong())
            fertileEnd = estimatedOvulation.plusDays((1 + marginDays / 2).toLong())
        }

        return CyclePrediction(
            averageCycleLengthDays = avgCycleLength,
            weightedCycleLengthDays = activeCycleLength,
            averagePeriodLengthDays = avgPeriodLength,
            customLutealLengthDays = customLutealLength,
            cycleVariationDays = sigma,
            hasAnomaly = hasAnomaly,
            confidenceLevel = confidenceLevel,
            confidenceWindowMarginDays = marginDays,
            totalCyclesRecorded = periodStarts.size,
            lastPeriodStartDate = lastPeriodStart,
            nextPredictedPeriodStartDate = nextPeriodStart,
            nextPredictedPeriodEndDate = nextPeriodEnd,
            estimatedOvulationDate = estimatedOvulation,
            fertileWindowStart = fertileStart,
            fertileWindowEnd = fertileEnd,
            isBasedOnCustomHistory = hasCustomHistory
        )
    }

    /**
     * Generates personalized recommendations based on symptoms recorded in the last 20 days,
     * plus targeted symptom-based recommendations accessible even without extraordinary symptoms.
     */
    fun generateRecommendationsForLast20Days(today: LocalDate = LocalDate.now()): List<Recommendation> {
        val startDate = today.minusDays(20)
        val logsLast20Days = dbHelper.getLogsForRange(startDate, today)

        val symptomsInLast20 = logsLast20Days.flatMap { it.symptoms }.toSet()
        val maxPain = logsLast20Days.maxOfOrNull { it.painIntensity } ?: 0
        val periodDaysCount = logsLast20Days.count { it.isPeriodDay }

        val recommendations = mutableListOf<Recommendation>()

        // 1. Red Flag Medical Alert
        val redFlagSymptoms = symptomsInLast20.filter { it.isRedFlag }
        if (redFlagSymptoms.isNotEmpty() || maxPain >= 5 || periodDaysCount > 8) {
            recommendations.add(
                Recommendation(
                    id = "rec_medical_alert",
                    title = "Atención Médica Prioritaria Recomendada",
                    description = "Has registrado síntomas atípicos relevantes en los últimos 20 días (${redFlagSymptoms.joinToString { it.nameEs }.ifEmpty { "Dolor severo o periodo muy prolongado" }}). Es aconsejable consultar con tu profesional de ginecología para una evaluación personalizada.",
                    priority = RecommendationPriority.MEDICAL_ALERT,
                    relatedSymptoms = redFlagSymptoms,
                    actionText = "Agendar consulta con ginecólogo"
                )
            )
        }

        // 2. Severe Cramps / Lumbar Pain
        if (symptomsInLast20.contains(Symptom.SEVERE_CRAMPS) || symptomsInLast20.contains(Symptom.LOWER_BACK_PAIN) || maxPain in 3..4) {
            recommendations.add(
                Recommendation(
                    id = "rec_pain_relief",
                    title = "Alivio para Cólicos y Dolor Lumbar",
                    description = "Se detectó incomodidad o dolor abdominal/lumbar en los últimos días. Aplica compresas de agua tibia en el bajo vientre durante 15 minutos, bebe infusión caliente de manzanilla/jengibre y realiza estiramientos suaves de cadera.",
                    priority = RecommendationPriority.RECOMMENDED,
                    relatedSymptoms = listOfNotNull(
                        if (symptomsInLast20.contains(Symptom.SEVERE_CRAMPS)) Symptom.SEVERE_CRAMPS else null,
                        if (symptomsInLast20.contains(Symptom.LOWER_BACK_PAIN)) Symptom.LOWER_BACK_PAIN else null
                    ),
                    actionText = "Aplicar calor local y descansar"
                )
            )
        }

        // 3. Bleeding Care
        if (symptomsInLast20.contains(Symptom.HEAVY_BLEEDING) || symptomsInLast20.contains(Symptom.SPOTTING) || symptomsInLast20.contains(Symptom.LARGE_CLOTS)) {
            recommendations.add(
                Recommendation(
                    id = "rec_bleeding_iron",
                    title = "Refuerzo de Hierro e Hidratación",
                    description = "Frente a flujo muy abundante o manchado reciente, incrementa el consumo de alimentos ricos en hierro (lentejas, espinaca, carnes magras) acompañados de cítricos (vitamina C) para su correcta absorción. Mantén un consumo regular de agua.",
                    priority = RecommendationPriority.RECOMMENDED,
                    relatedSymptoms = listOfNotNull(
                        if (symptomsInLast20.contains(Symptom.HEAVY_BLEEDING)) Symptom.HEAVY_BLEEDING else null,
                        if (symptomsInLast20.contains(Symptom.SPOTTING)) Symptom.SPOTTING else null
                    ),
                    actionText = "Aumentar hierro y líquidos"
                )
            )
        }

        // 4. Extreme Fatigue or Dizziness
        if (symptomsInLast20.contains(Symptom.EXTREME_FATIGUE) || symptomsInLast20.contains(Symptom.DIZZINESS)) {
            recommendations.add(
                Recommendation(
                    id = "rec_fatigue_rest",
                    title = "Protocolo de Recuperación y Energía",
                    description = "Registraste fatiga o mareos recientes. Prioriza descansos reparadores de 7 a 9 horas, evita cambios bruscos de postura y mantén un nivel adecuado de electrolitos en sangre.",
                    priority = RecommendationPriority.RECOMMENDED,
                    relatedSymptoms = listOfNotNull(
                        if (symptomsInLast20.contains(Symptom.EXTREME_FATIGUE)) Symptom.EXTREME_FATIGUE else null,
                        if (symptomsInLast20.contains(Symptom.DIZZINESS)) Symptom.DIZZINESS else null
                    ),
                    actionText = "Reposo guiado e hidratación"
                )
            )
        }

        // 5. Nausea or Bloating
        if (symptomsInLast20.contains(Symptom.NAUSEA) || symptomsInLast20.contains(Symptom.EXTREME_BLOATING)) {
            recommendations.add(
                Recommendation(
                    id = "rec_digestive_care",
                    title = "Cuidado Digestivo y Desinflamación",
                    description = "Detectamos hinchazón o náuseas en tus registros de las últimas 3 semanas. Consume alimentos ligeros en porciones pequeñas, reduce bebidas carbonatadas/sodio y prueba té de menta o jengibre.",
                    priority = RecommendationPriority.RECOMMENDED,
                    relatedSymptoms = listOfNotNull(
                        if (symptomsInLast20.contains(Symptom.NAUSEA)) Symptom.NAUSEA else null,
                        if (symptomsInLast20.contains(Symptom.EXTREME_BLOATING)) Symptom.EXTREME_BLOATING else null
                    ),
                    actionText = "Dieta blanda e infusiones"
                )
            )
        }

        // 6. Migraine
        if (symptomsInLast20.contains(Symptom.INTENSE_HEADACHE)) {
            recommendations.add(
                Recommendation(
                    id = "rec_migraine_care",
                    title = "Descanso Sensorial para Dolor de Cabeza",
                    description = "Ante las migrañas registradas, limita la exposición a pantallas, atenúa luces brillantes y descansa en un entorno fresco y silencioso. El masaje en sienes con lavanda también puede aliviar la tensión.",
                    priority = RecommendationPriority.RECOMMENDED,
                    relatedSymptoms = listOf(Symptom.INTENSE_HEADACHE),
                    actionText = "Pausa digital y luz tenue"
                )
            )
        }

        // 7. Mood / Mental Health
        val moodSymptoms = symptomsInLast20.filter { it.category == SymptomCategory.MOOD }
        if (moodSymptoms.isNotEmpty()) {
            recommendations.add(
                Recommendation(
                    id = "rec_mood_balance",
                    title = "Autorregulación y Cuidado Emocional",
                    description = "Has anotado fluctuaciones emocionales (${moodSymptoms.joinToString { it.nameEs }}). Reduce la ingesta de cafeína, realiza ejercicios de respiración lenta (4-7-8) y regálate espacios de relajación.",
                    priority = RecommendationPriority.RECOMMENDED,
                    relatedSymptoms = moodSymptoms,
                    actionText = "Respiración profunda 5 min"
                )
            )
        }

        // 8. Default clean status if NO symptoms were logged in the last 20 days
        if (recommendations.isEmpty()) {
            recommendations.add(
                Recommendation(
                    id = "rec_general_wellness",
                    title = "Sin Síntomas Atípicos Recientes",
                    description = "No has registrado síntomas atípicos ni molestias en los últimos 20 días. ¡Excelente! Mantén tu registro diario al día para seguir personalizando tu ciclo.",
                    priority = RecommendationPriority.WELLNESS_TIP,
                    relatedSymptoms = emptyList(),
                    actionText = "Registrar estado de hoy"
                )
            )
        }

        return recommendations
    }

    /**
     * Returns full catalog of targeted symptom recommendations for accessible viewing in menu.
     */
    fun getAllSymptomRecommendationsCatalog(): List<Recommendation> {
        return listOf(
            Recommendation(
                id = "cat_cramps",
                title = "Alivio para Cólicos y Dolor Pélvico",
                description = "Compresas tibias de 15 a 20 min en vientre bajo, infusión caliente de manzanilla/jengibre, masajes con aceite de lavanda y estiramientos suaves de cadera.",
                priority = RecommendationPriority.RECOMMENDED,
                relatedSymptoms = listOf(Symptom.SEVERE_CRAMPS, Symptom.LOWER_BACK_PAIN),
                actionText = "Calor local e infusión tibia"
            ),
            Recommendation(
                id = "cat_bleeding",
                title = "Cuidado por Flujo Abundante / Sangrado",
                description = "Aumenta la ingesta de hierro hemínico y vegetal (lentejas, espinaca, carnes) junto con vitamina C. Bebe abundante agua para reponer fluidos.",
                priority = RecommendationPriority.RECOMMENDED,
                relatedSymptoms = listOf(Symptom.HEAVY_BLEEDING, Symptom.SPOTTING),
                actionText = "Refuerzo de hierro e hidratación"
            ),
            Recommendation(
                id = "cat_fatigue",
                title = "Recuperación ante Fatiga y Mareos",
                description = "Prioriza 8 horas de sueño continuo, evita cambios bruscos de posición y consume agua con electrolitos o magnesio para mantener niveles de energía constantes.",
                priority = RecommendationPriority.RECOMMENDED,
                relatedSymptoms = listOf(Symptom.EXTREME_FATIGUE, Symptom.DIZZINESS),
                actionText = "Descanso reparador y electrolitos"
            ),
            Recommendation(
                id = "cat_digestive",
                title = "Desinflamación Digestiva y Náuseas",
                description = "Fracciona tus alimentos en porciones pequeñas, evita fritos o exceso de sodio y toma infusión de menta piperita o jengibre fresco.",
                priority = RecommendationPriority.RECOMMENDED,
                relatedSymptoms = listOf(Symptom.NAUSEA, Symptom.EXTREME_BLOATING),
                actionText = "Porciones pequeñas e infusión de menta"
            ),
            Recommendation(
                id = "cat_migraine",
                title = "Manejo Sensorial de Migraña y Dolor de Cabeza",
                description = "Descansa en habitación a oscuras, atenúa el volumen, aplica compresas frías en la frente o sienes y suspende el uso de pantallas digitales.",
                priority = RecommendationPriority.RECOMMENDED,
                relatedSymptoms = listOf(Symptom.INTENSE_HEADACHE),
                actionText = "Habitación oscura y compresa fría"
            ),
            Recommendation(
                id = "cat_mood",
                title = "Regulación Emocional e Irritabilidad",
                description = "Reduce la cafeína y ultraprocesados. Realiza técnica de respiración diafragmática 4-7-8 durante 5 minutos y practica pausas de autocuidado.",
                priority = RecommendationPriority.RECOMMENDED,
                relatedSymptoms = listOf(Symptom.MOOD_SWINGS, Symptom.ANXIETY),
                actionText = "Respiración 4-7-8 y pausa consciente"
            )
        )
    }
}
