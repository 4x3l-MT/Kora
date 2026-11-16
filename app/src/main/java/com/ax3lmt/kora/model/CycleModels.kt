package com.ax3lmt.kora.model

import java.time.LocalDate

enum class FlowLevel(val displayName: String) {
    NONE("Ninguno"),
    LIGHT("Ligero"),
    MEDIUM("Moderado"),
    HEAVY("Abundante")
}

enum class SymptomCategory(val displayName: String) {
    BLEEDING("Flujo y Sangrado Atípico"),
    PAIN("Dolor e Incomodidad"),
    PHYSICAL("Síntomas Físicos y Digestivos"),
    MOOD("Estado de Ánimo y Emocional")
}

enum class Symptom(
    val id: String,
    val nameEs: String,
    val category: SymptomCategory,
    val isRedFlag: Boolean = false
) {
    // Bleeding & Mucus
    HEAVY_BLEEDING("heavy_bleeding", "Sangrado Excesivo", SymptomCategory.BLEEDING, isRedFlag = false),
    SPOTTING("spotting", "Manchado Atípico / Intermenstrual", SymptomCategory.BLEEDING),
    LARGE_CLOTS("large_clots", "Coágulos Grandes", SymptomCategory.BLEEDING),
    LONG_PERIOD("long_period", "Periodo Prolongado (>7 días)", SymptomCategory.BLEEDING, isRedFlag = true),
    CERVICAL_MUCUS_EGG_WHITE("cervical_mucus", "Moco Cervical Elástico (Biomarcador Fértil)", SymptomCategory.BLEEDING),

    // Pain
    SEVERE_CRAMPS("severe_cramps", "Cólicos Intensos", SymptomCategory.PAIN),
    PELVIC_PAIN("pelvic_pain", "Dolor Pélvico Agudo", SymptomCategory.PAIN, isRedFlag = true),
    LOWER_BACK_PAIN("lower_back_pain", "Dolor Lumbar", SymptomCategory.PAIN),
    PAINFUL_URINATION("painful_urination", "Dolor al Orinar", SymptomCategory.PAIN, isRedFlag = true),
    PAINFUL_INTERCOURSE("painful_intercourse", "Dolor en Intimidad", SymptomCategory.PAIN),
    OVULATION_PAIN("ovulation_pain", "Dolor Ovulatorio (Mittelschmerz)", SymptomCategory.PAIN),

    // Physical, Digestive & Biomarkers
    FEVER("fever", "Fiebre", SymptomCategory.PHYSICAL, isRedFlag = true),
    DIZZINESS("dizziness", "Mareos / Vértigo", SymptomCategory.PHYSICAL),
    NAUSEA("nausea", "Náuseas / Vómitos", SymptomCategory.PHYSICAL),
    EXTREME_BLOATING("extreme_bloating", "Hinchazón Extrema", SymptomCategory.PHYSICAL),
    INTENSE_HEADACHE("intense_headache", "Migraña / Dolor de Cabeza", SymptomCategory.PHYSICAL),
    EXTREME_FATIGUE("extreme_fatigue", "Fatiga Extrema", SymptomCategory.PHYSICAL),
    POSITIVE_LH_TEST("positive_lh", "Test LH Positivo (Pico Ovulatorio)", SymptomCategory.PHYSICAL),
    BASAL_TEMP_RISE("temp_rise", "Elevación Temperatura Basal (BBT)", SymptomCategory.PHYSICAL),

    // Mood
    MOOD_SWINGS("mood_swings", "Cambios Drásticos de Humor", SymptomCategory.MOOD),
    ANXIETY("anxiety", "Ansiedad / Intranquilidad", SymptomCategory.MOOD),
    IRRITABILITY("irritability", "Irritabilidad", SymptomCategory.MOOD),
    SADNESS("sadness", "Tristeza / Bucle Emocional", SymptomCategory.MOOD);

    companion object {
        fun fromId(id: String): Symptom? = entries.find { it.id == id }
    }
}

data class DailyLog(
    val date: LocalDate,
    val isPeriodDay: Boolean = false,
    val flowLevel: FlowLevel = FlowLevel.NONE,
    val symptoms: Set<Symptom> = emptySet(),
    val painIntensity: Int = 0, // 0 to 5
    val notes: String = ""
)

enum class CyclePhase(val displayName: String, val description: String) {
    MENSTRUAL("Fase Menstrual", "Sangrado activo. Días de descanso y renovación."),
    FOLLICULAR("Fase Folicular", "Fase folicular (Pre-ovulación). Aumento de estrógenos y energía."),
    OVULATORY("Ventana Fértil", "Ventana fértil y ovulación estimada. Pico de vitalidad."),
    LUTEAL("Fase Lútea", "Fase lútea (Post-ovulación). Progesterona alta, calma y autocuidado.")
}

data class CyclePrediction(
    val averageCycleLengthDays: Int,
    val weightedCycleLengthDays: Int,
    val averagePeriodLengthDays: Int,
    val customLutealLengthDays: Int,
    val cycleVariationDays: Double,
    val hasAnomaly: Boolean,
    val confidenceLevel: String,
    val confidenceWindowMarginDays: Int,
    val totalCyclesRecorded: Int,
    val lastPeriodStartDate: LocalDate?,
    val nextPredictedPeriodStartDate: LocalDate?,
    val nextPredictedPeriodEndDate: LocalDate?,
    val estimatedOvulationDate: LocalDate?,
    val fertileWindowStart: LocalDate?,
    val fertileWindowEnd: LocalDate?,
    val isBasedOnCustomHistory: Boolean
) {
    fun getPhaseForDate(date: LocalDate, isConfirmedPeriod: Boolean = false): CyclePhase {
        if (isConfirmedPeriod) return CyclePhase.MENSTRUAL

        val nextStart = nextPredictedPeriodStartDate
        val nextEnd = nextPredictedPeriodEndDate
        if (nextStart != null && nextEnd != null && !date.isBefore(nextStart) && !date.isAfter(nextEnd)) {
            return CyclePhase.MENSTRUAL
        }

        val fertileStart = fertileWindowStart
        val fertileEnd = fertileWindowEnd
        if (fertileStart != null && fertileEnd != null && !date.isBefore(fertileStart) && !date.isAfter(fertileEnd)) {
            return CyclePhase.OVULATORY
        }

        if (fertileStart != null && lastPeriodStartDate != null) {
            val lastPeriodEnd = lastPeriodStartDate.plusDays((averagePeriodLengthDays - 1).coerceAtLeast(1).toLong())
            if (date.isAfter(lastPeriodEnd) && date.isBefore(fertileStart)) {
                return CyclePhase.FOLLICULAR
            }
        }

        if (fertileEnd != null && nextStart != null) {
            if (date.isAfter(fertileEnd) && date.isBefore(nextStart)) {
                return CyclePhase.LUTEAL
            }
        }

        // Relative fallback based on day in cycle if dates aren't in immediate next cycle range
        val refStart = lastPeriodStartDate ?: nextStart
        if (refStart != null) {
            val daysFromStart = java.time.temporal.ChronoUnit.DAYS.between(refStart, date)
            val cycleLength = if (weightedCycleLengthDays > 0) weightedCycleLengthDays else averageCycleLengthDays
            val cycleDay = Math.floorMod(daysFromStart, cycleLength.toLong()) + 1
            val ovDay = cycleLength - customLutealLengthDays
            return when {
                cycleDay <= averagePeriodLengthDays -> CyclePhase.MENSTRUAL
                cycleDay < (ovDay - 4) -> CyclePhase.FOLLICULAR
                cycleDay in (ovDay - 4)..(ovDay + 1) -> CyclePhase.OVULATORY
                else -> CyclePhase.LUTEAL
            }
        }

        return CyclePhase.FOLLICULAR
    }
}

enum class RecommendationPriority {
    MEDICAL_ALERT,
    RECOMMENDED,
    WELLNESS_TIP
}

data class Recommendation(
    val id: String,
    val title: String,
    val description: String,
    val priority: RecommendationPriority,
    val relatedSymptoms: List<Symptom> = emptyList(),
    val actionText: String
)
