package com.ax3lmt.kora

import com.ax3lmt.kora.model.DailyLog
import com.ax3lmt.kora.model.FlowLevel
import com.ax3lmt.kora.model.Symptom
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MenstrualPredictionTest {

    @Test
    fun testPredictionCalculationWithCustomData() {
        // User has 3 recorded periods:
        // Period 1: Jan 1 to Jan 5 (start Jan 1)
        // Period 2: Jan 29 to Feb 2 (start Jan 29, cycle length = 28 days)
        // Period 3: Feb 26 to Mar 2 (start Feb 26, cycle length = 28 days)
        val logs = listOf(
            DailyLog(LocalDate.of(2026, 1, 1), isPeriodDay = true, flowLevel = FlowLevel.MEDIUM),
            DailyLog(LocalDate.of(2026, 1, 2), isPeriodDay = true, flowLevel = FlowLevel.MEDIUM),
            DailyLog(LocalDate.of(2026, 1, 3), isPeriodDay = true, flowLevel = FlowLevel.LIGHT),
            DailyLog(LocalDate.of(2026, 1, 4), isPeriodDay = true, flowLevel = FlowLevel.LIGHT),

            DailyLog(LocalDate.of(2026, 1, 29), isPeriodDay = true, flowLevel = FlowLevel.HEAVY),
            DailyLog(LocalDate.of(2026, 1, 30), isPeriodDay = true, flowLevel = FlowLevel.MEDIUM),
            DailyLog(LocalDate.of(2026, 1, 31), isPeriodDay = true, flowLevel = FlowLevel.LIGHT),

            DailyLog(LocalDate.of(2026, 2, 26), isPeriodDay = true, flowLevel = FlowLevel.MEDIUM),
            DailyLog(LocalDate.of(2026, 2, 27), isPeriodDay = true, flowLevel = FlowLevel.MEDIUM),
            DailyLog(LocalDate.of(2026, 2, 28), isPeriodDay = true, flowLevel = FlowLevel.LIGHT)
        )

        val periodDays = logs.filter { it.isPeriodDay }.map { it.date }.sorted()
        val periodStarts = mutableListOf<LocalDate>()
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
                    currentStart = day
                    currentPrev = day
                }
            }
        }
        if (currentStart != null) periodStarts.add(currentStart)

        assertEquals(3, periodStarts.size)
        val cycleLength1 = ChronoUnit.DAYS.between(periodStarts[0], periodStarts[1]).toInt()
        val cycleLength2 = ChronoUnit.DAYS.between(periodStarts[1], periodStarts[2]).toInt()

        assertEquals(28, cycleLength1)
        assertEquals(28, cycleLength2)

        val avgCycle = listOf(cycleLength1, cycleLength2).average().toInt()
        val nextStart = periodStarts.last().plusDays(avgCycle.toLong())
        assertEquals(LocalDate.of(2026, 3, 26), nextStart)
    }

    @Test
    fun testSymptomRedFlagDetection() {
        val feverSymptom = Symptom.FEVER
        assertTrue(feverSymptom.isRedFlag)

        val severeCramps = Symptom.SEVERE_CRAMPS
        assertFalse(severeCramps.isRedFlag)
    }

    @Test
    fun testCyclePhaseClassification() {
        val prediction = com.ax3lmt.kora.model.CyclePrediction(
            averageCycleLengthDays = 28,
            weightedCycleLengthDays = 28,
            averagePeriodLengthDays = 5,
            customLutealLengthDays = 14,
            cycleVariationDays = 0.0,
            hasAnomaly = false,
            confidenceLevel = "Alta Precisión",
            confidenceWindowMarginDays = 0,
            totalCyclesRecorded = 1,
            lastPeriodStartDate = LocalDate.of(2026, 3, 1),
            nextPredictedPeriodStartDate = LocalDate.of(2026, 3, 29),
            nextPredictedPeriodEndDate = LocalDate.of(2026, 4, 2),
            estimatedOvulationDate = LocalDate.of(2026, 3, 15),
            fertileWindowStart = LocalDate.of(2026, 3, 11),
            fertileWindowEnd = LocalDate.of(2026, 3, 16),
            isBasedOnCustomHistory = true
        )

        // Day in confirmed period
        assertEquals(com.ax3lmt.kora.model.CyclePhase.MENSTRUAL, prediction.getPhaseForDate(LocalDate.of(2026, 3, 2), isConfirmedPeriod = true))
        // Day in follicular phase
        assertEquals(com.ax3lmt.kora.model.CyclePhase.FOLLICULAR, prediction.getPhaseForDate(LocalDate.of(2026, 3, 8)))
        // Day in fertile/ovulatory window
        assertEquals(com.ax3lmt.kora.model.CyclePhase.OVULATORY, prediction.getPhaseForDate(LocalDate.of(2026, 3, 14)))
        // Day in luteal phase
        assertEquals(com.ax3lmt.kora.model.CyclePhase.LUTEAL, prediction.getPhaseForDate(LocalDate.of(2026, 3, 22)))
    }
}
