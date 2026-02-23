package com.simats.nutrisoul

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class StressAndSleepViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(StressAndSleepUiState())
    val uiState: StateFlow<StressAndSleepUiState> = _uiState.asStateFlow()

    private val userAge = 25 // Placeholder for user age

    fun onReminderToggled(isEnabled: Boolean) {
        _uiState.update { it.copy(reminderEnabled = isEnabled) }
    }

    fun onEditScheduleClicked() {
        _uiState.update { it.copy(showSleepScheduleDialog = true) }
    }

    fun onDismissScheduleDialog() {
        _uiState.update { it.copy(showSleepScheduleDialog = false) }
    }

    fun onSaveSchedule(bedtime: LocalTime, wakeTime: LocalTime) {
        val newSchedule = SleepSchedule(bedtime = bedtime, wakeTime = wakeTime)
        _uiState.update { it.copy(sleepSchedule = newSchedule, showSleepScheduleDialog = false) }
        logSleep(newSchedule)
    }

    fun onLogTodaySleepClicked() {
        logSleep(_uiState.value.sleepSchedule)
    }

    private fun logSleep(schedule: SleepSchedule) {
        val (duration, durationMinutes) = calculateSleepDuration(schedule.bedtime, schedule.wakeTime)
        val quality = getSleepQualityByAge(durationMinutes, userAge)

        val newLog = SleepLog(
            date = LocalDate.now(),
            bedtime = schedule.bedtime,
            wakeTime = schedule.wakeTime,
            duration = duration,
            durationMinutes = durationMinutes,
            quality = quality
        )

        val updatedLogs = (_uiState.value.sleepLogs.filterNot { it.date.isEqual(newLog.date) } + newLog)
            .sortedByDescending { it.date }
            .take(7)

        _uiState.update { it.copy(sleepLogs = updatedLogs) }
        calculateWeeklyAverage(updatedLogs)
    }

    private fun calculateWeeklyAverage(logs: List<SleepLog>) {
        if (logs.isEmpty()) {
            _uiState.update { it.copy(weeklyAverageHours = 0f) }
            return
        }
        val totalMinutes = logs.sumOf { it.durationMinutes }
        val avgHours = (totalMinutes.toFloat() / logs.size) / 60f
        _uiState.update { it.copy(weeklyAverageHours = avgHours) }
    }

    fun onStartBreathing() {
        _uiState.update { it.copy(isBreathing = true) }
    }

    fun onStopBreathing() {
        _uiState.update { it.copy(isBreathing = false) }
    }

    private fun calculateSleepDuration(bedtime: LocalTime, wakeTime: LocalTime): Pair<String, Int> {
        val duration = if (wakeTime.isBefore(bedtime)) {
            Duration.between(bedtime, LocalTime.MAX).plus(Duration.between(LocalTime.MIN, wakeTime)).plusMinutes(1)
        } else {
            Duration.between(bedtime, wakeTime)
        }

        val durationMinutes = duration.toMinutes().toInt()
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        return "${hours}h ${minutes}m" to durationMinutes
    }

    private fun getSleepQualityByAge(durationMinutes: Int, age: Int): SleepQuality {
        val hours = durationMinutes / 60f
        val (normalMin, normalMax) = when {
            age >= 65 -> 7 to 8
            age >= 18 -> 7 to 9
            age >= 14 -> 8 to 10
            else -> 9 to 11
        }

        return when {
            hours >= normalMax + 2 -> SleepQuality.Over
            hours >= normalMin && hours <= normalMax -> SleepQuality.Good
            hours < normalMin -> SleepQuality.Poor
            else -> SleepQuality.Fair
        }
    }
}
