package com.simats.nutrisoul.ui.steps

data class StepsUiState(
    val todaySteps: Long = 0,
    val stepsGoal: Int = 10000,
    val weeklyAverage: Long = 0,
    val caloriesBurned: Int = 0,
    val distanceKm: Double = 0.0,
    val healthConnectStatus: HealthConnectStatus = HealthConnectStatus.NotInstalled,
    val hasPermissions: Boolean = false
)

enum class HealthConnectStatus {
    Installed, NotInstalled, NotAvailable
}

sealed class StepsScreenEvent {
    data class OnPermissionResult(val granted: Boolean) : StepsScreenEvent()
    object OnRequestPermissions : StepsScreenEvent()
    data class OnGoalSelected(val goal: Int) : StepsScreenEvent()
    object OnSyncSteps : StepsScreenEvent()
}
