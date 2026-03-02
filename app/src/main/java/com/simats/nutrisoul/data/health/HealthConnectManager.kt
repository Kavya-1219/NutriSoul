package com.simats.nutrisoul.data.health

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val availabilityStatus: Int
        get() = HealthConnectClient.getSdkStatus(context)

    fun getHealthConnectClient(): HealthConnectClient? {
        return if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
            _healthConnectClient
        } else {
            null
        }
    }

    fun isProviderAvailable(): Boolean {
        return availabilityStatus == HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun hasPermissions(permissions: Set<String>): Boolean {
        return _healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }
    
    fun installHealthConnect() {
        val intent = Intent("androidx.health.connect.action.HEALTH_CONNECT_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    suspend fun getTodaySteps(): Long {
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
        val now = Instant.now()
        
        val permissions = setOf(HealthPermission.getReadPermission(StepsRecord::class))
        if (!hasPermissions(permissions)) {
            return 0L
        }

        return try {
            val response = _healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startOfDay.toInstant(), now)
                )
            )
            response[StepsRecord.COUNT_TOTAL] ?: 0L
        } catch (e: Exception) {
            // Handle exceptions, e.g., user revokes permission
            0L
        }
    }
    
    companion object {
        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(StepsRecord::class)
        )
    }
}