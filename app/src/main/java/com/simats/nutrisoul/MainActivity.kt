package com.simats.nutrisoul

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.simats.nutrisoul.data.UserViewModel
import com.simats.nutrisoul.ui.theme.NutriSoulTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var initialSteps: Int = -1

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        setContent {
            val darkMode by userViewModel.darkMode.collectAsStateWithLifecycle()
            NutriSoulTheme(darkTheme = darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }

        lifecycleScope.launch {
            userViewModel.automaticTracking.collectLatest { enabled ->
                if (enabled && stepCounterSensor != null) {
                    registerStepSensor()
                } else {
                    unregisterStepSensor()
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val totalSteps = event.values[0].toInt()

        if (initialSteps == -1) {
            lifecycleScope.launch {
                val user = userViewModel.user.first()
                val stepsToday = user?.todaysSteps ?: 0
                initialSteps = totalSteps - stepsToday
            }
        }

        val newSteps = totalSteps - initialSteps
        userViewModel.updateStepsFromSensor(newSteps)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun registerStepSensor() {
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

    }

    private fun unregisterStepSensor() {
        sensorManager.unregisterListener(this)
        initialSteps = -1
    }
}
