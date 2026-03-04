package com.simats.nutrisoul.ui.steps

import android.app.Application
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.SessionManager
import com.simats.nutrisoul.data.repository.StepsRepository
import com.simats.nutrisoul.steps.StepTrackingService
import com.simats.nutrisoul.steps.StepsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StepsViewModel @Inject constructor(
    app: Application,
    private val stepsRepository: StepsRepository
) : AndroidViewModel(app) {

    private val store = StepsStore(app.applicationContext)
    private val sessionManager = SessionManager(app.applicationContext)

    private suspend fun currentUserKey(): String {
        return sessionManager.currentUserEmailFlow().first() ?: "guest"
    }

    val todaySteps: StateFlow<Int> =
        sessionManager.currentUserEmailFlow()
            .flatMapLatest { email -> store.todayStepsFlow(email ?: "guest") }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val autoEnabled: StateFlow<Boolean> =
        sessionManager.currentUserEmailFlow()
            .flatMapLatest { email -> store.autoEnabledFlow(email ?: "guest") }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val weeklyAverageSteps: StateFlow<Long> =
        stepsRepository.getWeeklySteps()
            .map { list ->
                if (list.isEmpty()) 0L
                else list.sumOf { it.steps } / list.size
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val isSensorAvailable: Boolean by lazy {
        val sensorManager = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
    }

    fun setAutoTracking(enabled: Boolean) {
        if (enabled && !isSensorAvailable) return // Defensive check

        val ctx = getApplication<Application>().applicationContext
        val intent = Intent(ctx, StepTrackingService::class.java)

        viewModelScope.launch {
            val userKey = currentUserKey()
            store.setAutoEnabled(userKey, enabled)
        }

        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        } else {
            ctx.stopService(intent)
        }
    }

    fun addManualSteps(steps: Int) {
        if (steps <= 0) return
        viewModelScope.launch {
            val userKey = currentUserKey()
            store.addManualSteps(userKey, steps)
        }
    }

    fun removeManualSteps(steps: Int) {
        if (steps <= 0) return
        viewModelScope.launch {
            val userKey = currentUserKey()
            store.removeManualSteps(userKey, steps)
        }
    }
}
