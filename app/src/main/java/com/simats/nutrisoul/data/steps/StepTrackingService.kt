package com.simats.nutrisoul.data.steps

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.simats.nutrisoul.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class StepTrackingService : Service() {

    private lateinit var stepSensorTracker: StepSensorTracker
    private lateinit var stepsStore: StepsStore
    private var sensorJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        stepSensorTracker = StepSensorTracker(this)
        stepsStore = StepsStore(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NutriSoul")
            .setContentText("Tracking your steps...")
            .setSmallIcon(R.drawable.nutrisoul)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        sensorJob = stepSensorTracker.steps.onEach { steps ->
            stepsStore.setTodaySteps("guest", steps)
        }.launchIn(CoroutineScope(Dispatchers.IO))

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stepSensorTracker.stop()
        sensorJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "step_tracking_channel"
        const val NOTIFICATION_ID = 1
    }
}