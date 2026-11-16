package com.ax3lmt.kora.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ax3lmt.kora.data.repository.MenstrualRepository
import com.ax3lmt.kora.model.CyclePrediction
import com.ax3lmt.kora.model.DailyLog
import com.ax3lmt.kora.model.FlowLevel
import com.ax3lmt.kora.model.Recommendation
import com.ax3lmt.kora.model.Symptom
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KoraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MenstrualRepository(application)

    private val _predictionState = MutableStateFlow(repository.calculatePrediction())
    val predictionState: StateFlow<CyclePrediction> = _predictionState.asStateFlow()

    private val _recommendationsState = MutableStateFlow(repository.generateRecommendationsForLast20Days())
    val recommendationsState: StateFlow<List<Recommendation>> = _recommendationsState.asStateFlow()

    private val _catalogRecommendationsState = MutableStateFlow(repository.getAllSymptomRecommendationsCatalog())
    val catalogRecommendationsState: StateFlow<List<Recommendation>> = _catalogRecommendationsState.asStateFlow()

    private val _allLogsState = MutableStateFlow<List<DailyLog>>(emptyList())
    val allLogsState: StateFlow<List<DailyLog>> = _allLogsState.asStateFlow()

    private val _selectedDateState = MutableStateFlow(LocalDate.now())
    val selectedDateState: StateFlow<LocalDate> = _selectedDateState.asStateFlow()

    private val _selectedLogState = MutableStateFlow<DailyLog?>(null)
    val selectedLogState: StateFlow<DailyLog?> = _selectedLogState.asStateFlow()

    private val _isOnboardingCompletedState = MutableStateFlow(repository.isOnboardingCompleted())
    val isOnboardingCompletedState: StateFlow<Boolean> = _isOnboardingCompletedState.asStateFlow()

    private val _defaultCycleDaysState = MutableStateFlow(repository.getDefaultCycleSetting())
    val defaultCycleDaysState: StateFlow<Int> = _defaultCycleDaysState.asStateFlow()

    private val _defaultPeriodDaysState = MutableStateFlow(repository.getDefaultPeriodSetting())
    val defaultPeriodDaysState: StateFlow<Int> = _defaultPeriodDaysState.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch(Dispatchers.IO) {
            val logs = repository.getAllLogs()
            _allLogsState.value = logs

            val prediction = repository.calculatePrediction()
            _predictionState.value = prediction

            val recommendations = repository.generateRecommendationsForLast20Days()
            _recommendationsState.value = recommendations

            _defaultCycleDaysState.value = repository.getDefaultCycleSetting()
            _defaultPeriodDaysState.value = repository.getDefaultPeriodSetting()

            selectDate(_selectedDateState.value)
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDateState.value = date
        viewModelScope.launch(Dispatchers.IO) {
            _selectedLogState.value = repository.getLog(date) ?: DailyLog(date = date)
        }
    }

    fun saveLog(
        date: LocalDate,
        isPeriodDay: Boolean,
        flowLevel: FlowLevel,
        symptoms: Set<Symptom>,
        painIntensity: Int,
        notes: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val log = DailyLog(
                date = date,
                isPeriodDay = isPeriodDay,
                flowLevel = flowLevel,
                symptoms = symptoms,
                painIntensity = painIntensity,
                notes = notes
            )
            repository.saveLog(log)
            _userMessage.value = "Registro guardado correctamente para el ${date}"
            refreshAll()
        }
    }

    fun deleteLog(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteLog(date)
            _userMessage.value = "Registro eliminado para el ${date}"
            refreshAll()
        }
    }

    fun completeInitialOnboarding(periodDays: Int?, cycleDays: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveInitialSetup(periodDays, cycleDays)
            _isOnboardingCompletedState.value = true
            _userMessage.value = "¡Bienvenida a Kora! Configuración completada."
            refreshAll()
        }
    }

    fun updateSettings(cycleDays: Int, periodDays: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveDefaultCycleSetting(cycleDays)
            repository.saveDefaultPeriodSetting(periodDays)
            _userMessage.value = "Configuración predeterminada actualizada"
            refreshAll()
        }
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllData()
            _userMessage.value = "Todos los datos locales han sido borrados"
            refreshAll()
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
