package com.simats.nutrisoul

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!MindCarePrefs.loadReminderEnabled(context)) return
        val schedule = MindCarePrefs.loadSchedule(context)
        scheduleBedtimeReminder(context, schedule.bedtime)
    }
}
