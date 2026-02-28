package com.simats.nutrisoul

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
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

    fun onSaveSchedule(bedtime: LocalTime, wakeTime: LocalTime, context: Context) {
        val newSchedule = SleepSchedule(bedtime, wakeTime)
        _uiState.update {
            it.copy(
                sleepSchedule = newSchedule,
                showSleepScheduleDialog = false
            )
        }
        if (uiState.value.reminderEnabled) {
            scheduleBedtimeReminder(context, bedtime)
        }
    }

    fun onEditScheduleClicked() {
        _uiState.update { it.copy(showSleepScheduleDialog = true) }
    }

    fun onLogTodaySleepClicked() {
        _uiState.update { it.copy(showLogSleepDialog = true) }
    }

    fun onDismissLogSleepDialog() {
        _uiState.update { it.copy(showLogSleepDialog = false) }
    }

    fun onLogSleep(bedtime: LocalTime, wakeTime: LocalTime, quality: SleepQuality) {
        val duration = if (wakeTime.isBefore(bedtime)) {
            Duration.between(bedtime, wakeTime).plusHours(24)
        } else {
            Duration.between(bedtime, wakeTime)
        }
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val durationString = "${hours}h ${minutes}m"

        val newLog = SleepLog(
            date = LocalDate.now(),
            bedtime = bedtime,
            wakeTime = wakeTime,
            duration = durationString,
            durationMinutes = duration.toMinutes().toInt(),
            quality = quality
        )

        _uiState.update {
            it.copy(
                sleepLogs = listOf(newLog) + it.sleepLogs.filter { log -> !log.date.isEqual(LocalDate.now()) },
                showLogSleepDialog = false
            )
        }
    }

    fun onReminderToggled(enabled: Boolean, context: Context) {
        _uiState.update { it.copy(reminderEnabled = enabled) }
        if (enabled) {
            scheduleBedtimeReminder(context, uiState.value.sleepSchedule.bedtime)
        } else {
            cancelBedtimeReminder(context)
        }
    }

    fun onStartBreathing() {
        _uiState.update { it.copy(isBreathing = true) }
    }

    fun onStopBreathing() {
        _uiState.update { it.copy(isBreathing = false) }
    }

    fun onShowWindDownDialog() {
        _uiState.update { it.copy(showWindDownDialog = true) }
    }

    fun onDismissWindDownDialog() {
        _uiState.update { it.copy(showWindDownDialog = false) }
    }
}

val dummySleepLogs = listOf(
    SleepLog(LocalDate.now().minusDays(1), LocalTime.of(22, 45), LocalTime.of(6, 15), "7h 30m", 450, SleepQuality.Good),
    SleepLog(LocalDate.now().minusDays(2), LocalTime.of(23, 30), LocalTime.of(6, 15), "6h 45m", 405, SleepQuality.Fair),
    SleepLog(LocalDate.now().minusDays(3), LocalTime.of(22, 0), LocalTime.of(6, 0), "8h 0m", 480, SleepQuality.Good),
    SleepLog(LocalDate.now().minusDays(4), LocalTime.of(1, 0), LocalTime.of(6, 30), "5h 30m", 330, SleepQuality.Poor),
)

fun cancelBedtimeReminder(context: Context) {
    val intent = Intent(context, BedtimeReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE)
    if (pendingIntent != null) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}
