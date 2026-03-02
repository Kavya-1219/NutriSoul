package com.simats.nutrisoul.steps

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore("steps_store")

class StepsStore(private val context: Context) {

    private fun keyTodaySteps(user: String) = intPreferencesKey("today_steps_$user")
    private fun keyDate(user: String) = stringPreferencesKey("steps_date_$user")
    private fun keyBaseline(user: String) = floatPreferencesKey("steps_baseline_$user")
    private fun keyLastTotal(user: String) = floatPreferencesKey("steps_last_total_$user")
    private fun keyAuto(user: String) = booleanPreferencesKey("auto_enabled_$user")

    fun todayStepsFlow(user: String): Flow<Int> =
        context.dataStore.data.map { it[keyTodaySteps(user)] ?: 0 }

    fun autoEnabledFlow(user: String): Flow<Boolean> =
        context.dataStore.data.map { it[keyAuto(user)] ?: false }

    suspend fun setAutoEnabled(user: String, enabled: Boolean) {
        context.dataStore.edit { it[keyAuto(user)] = enabled }
    }

    suspend fun updateFromStepCounter(user: String, totalSinceBoot: Float) {
        val today = LocalDate.now().toString()

        context.dataStore.edit { prefs ->
            val savedDate = prefs[keyDate(user)]
            var baseline = prefs[keyBaseline(user)]
            val lastTotal = prefs[keyLastTotal(user)]

            // New day => reset baseline
            if (savedDate == null || savedDate != today) {
                prefs[keyDate(user)] = today
                prefs[keyBaseline(user)] = totalSinceBoot
                prefs[keyLastTotal(user)] = totalSinceBoot
                prefs[keyTodaySteps(user)] = 0
                return@edit
            }

            // Reboot detection (sensor total resets)
            if (lastTotal != null && totalSinceBoot < lastTotal) {
                prefs[keyBaseline(user)] = totalSinceBoot
                baseline = totalSinceBoot
            }

            if (baseline == null) {
                prefs[keyBaseline(user)] = totalSinceBoot
                baseline = totalSinceBoot
            }

            val steps = (totalSinceBoot - baseline).toInt().coerceAtLeast(0)
            prefs[keyTodaySteps(user)] = steps
            prefs[keyLastTotal(user)] = totalSinceBoot
        }
    }

    suspend fun addManualSteps(user: String, add: Int) {
        if (add <= 0) return
        context.dataStore.edit { prefs ->
            val current = prefs[keyTodaySteps(user)] ?: 0
            prefs[keyTodaySteps(user)] = current + add
        }
    }

    suspend fun removeManualSteps(user: String, remove: Int) {
        if (remove <= 0) return
        context.dataStore.edit { prefs ->
            val current = prefs[keyTodaySteps(user)] ?: 0
            prefs[keyTodaySteps(user)] = (current - remove).coerceAtLeast(0)
        }
    }
}
