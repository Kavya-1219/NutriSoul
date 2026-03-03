package com.simats.nutrisoul.steps

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.compose.ui.Alignment
import androidx.core.app.NotificationCompat
import com.simats.nutrisoul.R
import com.simats.nutrisoul.data.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class StepTrackingService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounter: Sensor? = null

    private lateinit var store: StepsStore
    private lateinit var sessionManager: SessionManager

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        store = StepsStore(applicationContext)
        sessionManager = SessionManager(applicationContext)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        startForeground(NOTIFICATION_ID, buildNotification("Tracking your steps..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (stepCounter == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI)
        return START_STICKY
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val totalSinceBoot = event.values.firstOrNull() ?: return

        scope.launch {
            val email = sessionManager.currentUserEmailFlow().first() ?: return@launch

            // Convert "since boot" counter -> "today steps"
            // StepsStore must handle baseline + daily reset correctly
            // In onSensorChanged
            store.updateFromStepCounter(
                user = email,
                totalSinceBoot = totalSinceBoot
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(text: String): Notification {
        val channelId = CHANNEL_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Step Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("NutriSoul")
            .setContentText(text)
            .setSmallIcon(R.drawable.nutrisoul) // use your app icon
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "steps_tracking"
        private const val NOTIFICATION_ID = 1001
    }
}
