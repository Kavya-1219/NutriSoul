package com.simats.nutrisoul

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReminderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WindDownDialog(
                onStartWindDown = {
                    // Navigate to breathing exercise
                    finish()
                },
                onSnooze = {
                    // Snooze for 10 minutes
                    finish()
                }
            )
        }
    }
}
