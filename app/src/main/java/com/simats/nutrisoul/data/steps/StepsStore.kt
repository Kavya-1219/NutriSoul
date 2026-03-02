package com.simats.nutrisoul.data.steps

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("steps_store")

class StepsStore(private val context: Context) {

    private fun key(user: String) = intPreferencesKey("today_steps_$user")

    fun todayStepsFlow(user: String): Flow<Int> =
        context.dataStore.data.map { it[key(user)] ?: 0 }

    suspend fun setTodaySteps(user: String, value: Int) {
        context.dataStore.edit { it[key(user)] = value }
    }

    suspend fun addSteps(user: String, add: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[key(user)] ?: 0
            prefs[key(user)] = (current + add).coerceAtLeast(0)
        }
    }

    suspend fun removeSteps(user: String, remove: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[key(user)] ?: 0
            prefs[key(user)] = (current - remove).coerceAtLeast(0)
        }
    }
}