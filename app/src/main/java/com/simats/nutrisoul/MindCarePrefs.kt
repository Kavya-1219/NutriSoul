package com.simats.nutrisoul

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

object MindCarePrefs {
    private const val PREF = "mind_care_prefs"
    private const val KEY_BEDTIME = "bedtime"
    private const val KEY_WAKETIME = "waketime"
    private const val KEY_REMINDER = "reminder_enabled"
    private const val KEY_LOGS = "sleep_logs"
    private const val KEY_PENDING_WINDDOWN = "pending_winddown"
    private const val KEY_SNOOZE_UNTIL = "snooze_until"

    fun saveSchedule(context: Context, schedule: SleepSchedule) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit()
            .putString(KEY_BEDTIME, schedule.bedtime.toString())   // "22:00"
            .putString(KEY_WAKETIME, schedule.wakeTime.toString()) // "06:00"
            .apply()
    }

    fun loadSchedule(context: Context): SleepSchedule {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val bed = sp.getString(KEY_BEDTIME, "22:00") ?: "22:00"
        val wake = sp.getString(KEY_WAKETIME, "06:00") ?: "06:00"
        return SleepSchedule(LocalTime.parse(bed), LocalTime.parse(wake))
    }

    fun saveReminderEnabled(context: Context, enabled: Boolean) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_REMINDER, enabled).apply()
    }

    fun loadReminderEnabled(context: Context): Boolean {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_REMINDER, false)
    }

    fun saveLogs(context: Context, logs: List<SleepLog>) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val arr = JSONArray()
        logs.sortedByDescending { it.date }.take(7).forEach { log ->
            val obj = JSONObject()
            obj.put("date", log.date.toString())
            obj.put("bedtime", log.bedtime.toString())
            obj.put("wakeTime", log.wakeTime.toString())
            obj.put("duration", log.duration)
            obj.put("durationMinutes", log.durationMinutes)
            obj.put("quality", log.quality.name)
            arr.put(obj)
        }
        sp.edit().putString(KEY_LOGS, arr.toString()).apply()
    }

    fun loadLogs(context: Context): List<SleepLog> {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val raw = sp.getString(KEY_LOGS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<SleepLog>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val date = LocalDate.parse(obj.getString("date"))
                val bedtime = LocalTime.parse(obj.getString("bedtime"))
                val wakeTime = LocalTime.parse(obj.getString("wakeTime"))
                val duration = obj.getString("duration")
                val durationMinutes = obj.getInt("durationMinutes")
                val quality = SleepQuality.valueOf(obj.getString("quality"))
                list.add(SleepLog(date, bedtime, wakeTime, duration, durationMinutes, quality))
            }
            list.sortedByDescending { it.date }.take(7)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun setPendingWindDown(context: Context, pending: Boolean) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_PENDING_WINDDOWN, pending).apply()
    }

    fun consumePendingWindDown(context: Context): Boolean {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val pending = sp.getBoolean(KEY_PENDING_WINDDOWN, false)
        if (pending) sp.edit().putBoolean(KEY_PENDING_WINDDOWN, false).apply()
        return pending
    }

    fun saveSnoozeUntil(context: Context, millis: Long) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit().putLong(KEY_SNOOZE_UNTIL, millis).apply()
    }

    fun getSnoozeUntil(context: Context): Long {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return sp.getLong(KEY_SNOOZE_UNTIL, 0L)
    }

    fun clearSnooze(context: Context) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit().remove(KEY_SNOOZE_UNTIL).apply()
    }
}
