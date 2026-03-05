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
        val userEmail = intent?.getStringExtra("USER_EMAIL") ?: return

        // If user snoozed and snooze hasn't expired, skip showing again.
        val snoozeUntil = MindCarePrefs.getSnoozeUntil(context, userEmail)
        if (snoozeUntil > System.currentTimeMillis()) {
            rescheduleNext(context, userEmail)
            return
        }

        // Mark wind-down pending so next time user opens Mind Care screen, popup shows
        MindCarePrefs.setPendingWindDown(context, userEmail, true)

        // Launch full-screen alarm UI
        launchAlarmActivity(context, userEmail)

        // Full-screen notification
        showFullScreenNotification(context, userEmail)

        // Reschedule next day
        rescheduleNext(context, userEmail)
    }

    private fun rescheduleNext(context: Context, userEmail: String) {
        if (!MindCarePrefs.loadReminderEnabled(context, userEmail)) return
        val schedule = MindCarePrefs.loadSchedule(context, userEmail)
        scheduleBedtimeReminder(context, userEmail, schedule.bedtime)
    }

    private fun launchAlarmActivity(context: Context, userEmail: String) {
        val i = Intent(context, BedtimeAlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("USER_EMAIL", userEmail)
        }
        context.startActivity(i)
    }

    private fun showFullScreenNotification(context: Context, userEmail: String) {
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
            putExtra("USER_EMAIL", userEmail)
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
