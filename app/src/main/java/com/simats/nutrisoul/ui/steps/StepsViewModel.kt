package com.simats.nutrisoul.ui.steps

import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.health.HealthConnectManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StepsViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StepsUiState())
    val uiState: StateFlow<StepsUiState> = _uiState

    init {
        viewModelScope.launch {
            val status = when (healthConnectManager.availabilityStatus) {
                HealthConnectClient.SDK_AVAILABLE -> HealthConnectStatus.Installed
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectStatus.NotInstalled
                else -> HealthConnectStatus.NotAvailable
            }
            val hasPermissions = if (status == HealthConnectStatus.Installed) {
                healthConnectManager.hasPermissions(HealthConnectManager.PERMISSIONS)
            } else {
                false
            }
            _uiState.update {
                it.copy(
                    healthConnectStatus = status,
                    hasPermissions = hasPermissions
                )
            }
        }
    }

    fun onEvent(event: StepsScreenEvent) {
        when (event) {
            is StepsScreenEvent.OnPermissionResult -> {
                _uiState.value = _uiState.value.copy(hasPermissions = event.granted)
                if (event.granted) {
                    // Optionally refresh data
                }
            }
            is StepsScreenEvent.OnGoalSelected -> {
                _uiState.value = _uiState.value.copy(stepsGoal = event.goal)
            }
            else -> {}
        }
    }
}