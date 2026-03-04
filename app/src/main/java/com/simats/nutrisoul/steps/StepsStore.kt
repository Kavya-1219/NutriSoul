package com.simats.nutrisoul.steps

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore("steps_store")

class StepsStore(private val context: Context) {

    private fun safeUserKey(user: String) = user.replace("@", "_at_").replace(".", "_")

    private fun keyDate(user: String) = stringPreferencesKey("steps_date_${safeUserKey(user)}")
    
    // ✅ Renamed keys to avoid ClassCastException from old Float data
    private fun keyBaseline(user: String) = longPreferencesKey("steps_baseline_l_${safeUserKey(user)}")
    private fun keyLastTotal(user: String) = longPreferencesKey("steps_last_total_l_${safeUserKey(user)}")
    
    private fun keyAutoEnabled(user: String) = booleanPreferencesKey("auto_enabled_${safeUserKey(user)}")

    // ✅ split auto + manual to avoid overwrite
    private fun keyAutoSteps(user: String) = intPreferencesKey("auto_steps_${safeUserKey(user)}")
    private fun keyManualSteps(user: String) = intPreferencesKey("manual_steps_${safeUserKey(user)}")

    // optional: last manual change (for showing “recent activity”)
    private fun keyLastManualDelta(user: String) = intPreferencesKey("manual_last_delta_${safeUserKey(user)}")

    fun autoEnabledFlow(user: String): Flow<Boolean> =
        context.dataStore.data.map { it[keyAutoEnabled(user)] ?: false }

    fun todayStepsFlow(user: String): Flow<Int> =
        context.dataStore.data.map { prefs ->
            val auto = prefs[keyAutoSteps(user)] ?: 0
            val manual = prefs[keyManualSteps(user)] ?: 0
            (auto + manual).coerceAtLeast(0)
        }

    fun manualStepsFlow(user: String): Flow<Int> =
        context.dataStore.data.map { it[keyManualSteps(user)] ?: 0 }

    suspend fun setAutoEnabled(user: String, enabled: Boolean) {
        context.dataStore.edit { it[keyAutoEnabled(user)] = enabled }
    }

    suspend fun cleanupOldFloatKeys(user: String) {
        val oldBaseline = floatPreferencesKey("steps_baseline_${user}")
        val oldLastTotal = floatPreferencesKey("steps_last_total_${user}")
        val oldBaselineSanitized = floatPreferencesKey("steps_baseline_${safeUserKey(user)}")
        val oldLastTotalSanitized = floatPreferencesKey("steps_last_total_${safeUserKey(user)}")

        context.dataStore.edit { prefs ->
            prefs.remove(oldBaseline)
            prefs.remove(oldLastTotal)
            prefs.remove(oldBaselineSanitized)
            prefs.remove(oldLastTotalSanitized)
        }
    }

    /**
     * Called from StepTrackingService using TYPE_STEP_COUNTER.
     * Stores AUTO steps only. UI reads auto+manual combined.
     * Returns previous day's data if a reset occurred.
     */
    suspend fun updateFromStepCounter(user: String, totalSinceBoot: Long): Pair<String, Int>? {
        val today = LocalDate.now().toString()
        var previousDayData: Pair<String, Int>? = null

        context.dataStore.edit { prefs ->
            val savedDate = prefs[keyDate(user)]
            var baseline = prefs[keyBaseline(user)]
            val lastTotal = prefs[keyLastTotal(user)]

            if (savedDate == null || savedDate != today) {
                // Save previous day data before resetting
                if (savedDate != null) {
                    val auto = prefs[keyAutoSteps(user)] ?: 0
                    val manual = prefs[keyManualSteps(user)] ?: 0
                    previousDayData = Pair(savedDate, auto + manual)
                }

                prefs[keyDate(user)] = today
                prefs[keyBaseline(user)] = totalSinceBoot
                prefs[keyLastTotal(user)] = totalSinceBoot
                prefs[keyAutoSteps(user)] = 0
                prefs[keyManualSteps(user)] = 0
                prefs[keyLastManualDelta(user)] = 0
                return@edit
            }

            // Reboot detection (only if it REALLY decreases)
            if (lastTotal != null && totalSinceBoot < lastTotal) {
                prefs[keyBaseline(user)] = totalSinceBoot
                baseline = totalSinceBoot
            }

            if (baseline == null) {
                prefs[keyBaseline(user)] = totalSinceBoot
                baseline = totalSinceBoot
            }

            val autoSteps = (totalSinceBoot - baseline).toInt().coerceAtLeast(0)
            prefs[keyAutoSteps(user)] = autoSteps
            prefs[keyLastTotal(user)] = totalSinceBoot
        }
        return previousDayData
    }

    suspend fun addManualSteps(user: String, add: Int) {
        if (add <= 0) return
        context.dataStore.edit { prefs ->
            val current = prefs[keyManualSteps(user)] ?: 0
            prefs[keyManualSteps(user)] = (current + add).coerceAtLeast(0)
            prefs[keyLastManualDelta(user)] = add
        }
    }

    suspend fun removeManualSteps(user: String, remove: Int) {
        if (remove <= 0) return
        context.dataStore.edit { prefs ->
            val current = prefs[keyManualSteps(user)] ?: 0
            prefs[keyManualSteps(user)] = (current - remove).coerceAtLeast(0)
            prefs[keyLastManualDelta(user)] = -remove
        }
    }
}
