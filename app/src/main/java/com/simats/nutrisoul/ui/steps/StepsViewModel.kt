package com.simats.nutrisoul.ui.steps

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.SessionManager
import com.simats.nutrisoul.steps.StepTrackingService
import com.simats.nutrisoul.steps.StepsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StepsViewModel @Inject constructor(app: Application) : AndroidViewModel(app) {

    private val store = StepsStore(app.applicationContext)
    private val sessionManager = SessionManager(app.applicationContext)

    val todaySteps: StateFlow<Int> = sessionManager.currentUserEmailFlow().flatMapLatest { email ->
        if (email != null) {
            store.todayStepsFlow(email)
        } else {
            store.todayStepsFlow("guest")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val autoEnabled: StateFlow<Boolean> = sessionManager.currentUserEmailFlow().flatMapLatest { email ->
        if (email != null) {
            store.autoEnabledFlow(email)
        } else {
            store.autoEnabledFlow("guest")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setAutoTracking(enabled: Boolean) {
        viewModelScope.launch {
            val email = sessionManager.currentUserEmailFlow().first()
            if (email != null) {
                store.setAutoEnabled(email, enabled)
            }
        }

        val ctx = getApplication<Application>().applicationContext
        val intent = Intent(ctx, StepTrackingService::class.java)

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
        viewModelScope.launch {
            val email = sessionManager.currentUserEmailFlow().first()
            if (email != null) {
                store.addManualSteps(email, steps)
            }
        }
    }
}
