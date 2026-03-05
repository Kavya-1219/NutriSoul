package com.simats.nutrisoul

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class BedtimeReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        // Snooze check
        val snoozeUntil = MindCarePrefs.getSnoozeUntil(context)
        if (snoozeUntil > System.currentTimeMillis()) {
            rescheduleNext(context)
            return
        }

        // Set pending (so MindCare shows dialog when opened)
        MindCarePrefs.setPendingWindDown(context, true)

        // Launch full-screen alarm UI (works when using other apps + lock screen)
        launchAlarmActivity(context)

        // Full-screen notification (backup + required for modern UX)
        showFullScreenNotification(context)

        // Reschedule next day
        rescheduleNext(context)
    }

    private fun rescheduleNext(context: Context) {
        if (!MindCarePrefs.loadReminderEnabled(context)) return
        val schedule = MindCarePrefs.loadSchedule(context)
        scheduleBedtimeReminder(context, schedule.bedtime)
    }

    private fun launchAlarmActivity(context: Context) {
        val i = Intent(context, BedtimeAlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(i)
    }

    private fun showFullScreenNotification(context: Context) {
        val channelId = "bedtime_reminders"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "Bedtime Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        val fullScreenIntent = Intent(context, BedtimeAlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val fullScreenPI = PendingIntent.getActivity(
            context,
            9101,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.nutrisoul)
            .setContentTitle("It’s bedtime 🌙")
            .setContentText("Tap to start wind-down.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPI, true)
            .build()

        nm.notify(9101, notification)
    }
}
