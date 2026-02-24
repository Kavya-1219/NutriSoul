package com.simats.nutrisoul

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class StressAndSleepViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(StressAndSleepUiState())
    val uiState: StateFlow<StressAndSleepUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    sleepLogs = dummySleepLogs,
                    weeklyAverageHours = 7.5f
                )
            }
        }
    }

    fun onDismissScheduleDialog() {
        _uiState.update { it.copy(showSleepScheduleDialog = false) }
    }

    fun onSaveSchedule(bedtime: LocalTime, wakeTime: LocalTime) {
        _uiState.update {
            it.copy(
                sleepSchedule = SleepSchedule(bedtime, wakeTime),
                showSleepScheduleDialog = false
            )
        }
    }

    fun onEditScheduleClicked() {
        _uiState.update { it.copy(showSleepScheduleDialog = true) }
    }

    fun onLogTodaySleepClicked() {
        // Not implemented
    }

    fun onReminderToggled(enabled: Boolean) {
        _uiState.update { it.copy(reminderEnabled = enabled) }
    }

    fun onStartBreathing() {
        _uiState.update { it.copy(isBreathing = true) }
    }

    fun onStopBreathing() {
        _uiState.update { it.copy(isBreathing = false) }
    }
}

val dummySleepLogs = listOf(
    SleepLog(LocalDate.now(), LocalTime.of(22, 15), LocalTime.of(6, 30), "8h 15m", 495, SleepQuality.Good),
    SleepLog(LocalDate.now().minusDays(1), LocalTime.of(22, 45), LocalTime.of(6, 15), "7h 30m", 450, SleepQuality.Good),
    SleepLog(LocalDate.now().minusDays(2), LocalTime.of(23, 30), LocalTime.of(6, 15), "6h 45m", 405, SleepQuality.Fair),
    SleepLog(LocalDate.now().minusDays(3), LocalTime.of(22, 0), LocalTime.of(6, 0), "8h 0m", 480, SleepQuality.Good),
    SleepLog(LocalDate.now().minusDays(4), LocalTime.of(1, 0), LocalTime.of(6, 30), "5h 30m", 330, SleepQuality.Poor),
)
