@file:OptIn(ExperimentalMaterial3Api::class)
package com.simats.nutrisoul

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.simats.nutrisoul.data.User
import com.simats.nutrisoul.data.UserViewModel
import java.text.NumberFormat
import java.util.Locale

// -------------------------------
//    MAIN SCREEN
// -------------------------------
@Composable
fun StepsTrackingScreen(navController: NavController, userViewModel: UserViewModel) {

    val user by userViewModel.user.collectAsStateWithLifecycle()
    val automaticTracking by userViewModel.automaticTracking.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            userViewModel.setAutomaticTracking(true)
        }
    }

    // States
    var selectedGoal by remember { mutableStateOf("5k") }
    var showManualEntryDialog by remember { mutableStateOf(false) }
    var showAddStepsChoiceDialog by remember { mutableStateOf(false) }
    var showRemoveStepsDialog by remember { mutableStateOf(false) }

    val goalInSteps = selectedGoal.replace("k", "000").toInt()

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00C853))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            StepsHeader(navController)

            // White container
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                user?.let { StepsProgress(it.todaysSteps, goalInSteps) }

                TrackingModeSwitch(
                    automaticTracking = automaticTracking,
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACTIVITY_RECOGNITION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                userViewModel.setAutomaticTracking(true)
                            } else {
                                permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                            }
                        } else {
                            userViewModel.setAutomaticTracking(false)
                        }
                    }
                )

                MotivationMessage()

                user?.let { InfoBoxes(it.todaysSteps) }

                DailyGoal(selectedGoal) { selectedGoal = it }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AddStepsButton(
                        modifier = Modifier.weight(1f),
                        onClick = { showAddStepsChoiceDialog = true }
                    )
                    RemoveStepsButton(
                        modifier = Modifier.weight(1f),
                        onClick = { showRemoveStepsDialog = true }
                    )
                }

                TipsToIncreaseSteps()
            }
        }
    }

    // Add steps choice dialog
    if (showAddStepsChoiceDialog) {
        AddStepsChoiceDialog(
            onDismiss = { showAddStepsChoiceDialog = false },
            onAddFromGoal = {
                userViewModel.updateSteps(goalInSteps)
                showAddStepsChoiceDialog = false
            },
            onEnterManually = {
                showAddStepsChoiceDialog = false
                showManualEntryDialog = true
            }
        )
    }

    // Manual entry dialog
    if (showManualEntryDialog) {
        ManualStepsDialog(
            onDismiss = { showManualEntryDialog = false },
            onSubmit = { addedSteps ->
                userViewModel.updateSteps(addedSteps)
                showManualEntryDialog = false
            },
            showHonestyMessage = true
        )
    }

    if (showRemoveStepsDialog) {
        ManualStepsDialog(
            onDismiss = { showRemoveStepsDialog = false },
            onSubmit = { removedSteps ->
                userViewModel.updateSteps(-removedSteps)
                showRemoveStepsDialog = false
            },
            title = "Remove Steps Manually",
            submitButtonText = "Remove",
            showHonestyMessage = false
        )
    }
}

// -------------------------------
//    HEADER
// -------------------------------
@Composable
private fun StepsHeader(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Column {
            Text(
                text = "Steps Tracking",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Every step brings you closer!",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp
            )
        }
    }
}

// -------------------------------
//    CIRCULAR PROGRESS
// -------------------------------
@Composable
private fun StepsProgress(steps: Int, goalInSteps: Int) {

    val progress = (steps.toFloat() / goalInSteps).coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()
    val formattedSteps = NumberFormat.getNumberInstance(Locale.US).format(steps)

    Box(
        modifier = Modifier
            .size(220.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 16.dp,
            color = Color(0xFF00C853),
            trackColor = Color.LightGray.copy(alpha = 0.4f)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.DirectionsWalk,
                contentDescription = null,
                tint = Color(0xFF00C853),
                modifier = Modifier.size(40.dp)
            )
            Text(
                formattedSteps,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Text("steps", color = Color.Gray)
            Text("$percentage %", color = Color.Gray)
        }
    }
}

// -------------------------------
//   AUTO TRACKING SWITCH
// -------------------------------
@Composable
private fun TrackingModeSwitch(automaticTracking: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Automatic Tracking", fontWeight = FontWeight.Medium)
            Switch(checked = automaticTracking, onCheckedChange = onCheckedChange)
        }
    }
}

// -------------------------------
//    MESSAGE CARD
// -------------------------------
@Composable
private fun MotivationMessage() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFC107))
            Spacer(Modifier.width(8.dp))
            Text("Let's get moving today!", fontWeight = FontWeight.SemiBold)
        }
    }
}

// -------------------------------
//    INFO BOXES (kcal, km, avg)
// -------------------------------
@Composable
private fun InfoBoxes(steps: Int) {

    val calories = (steps * 0.05).toInt()
    val distance = steps * 0.0008

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        InfoBox(
            value = calories.toString(),
            label = "kcal",
            icon = Icons.Default.LocalFireDepartment,
            iconColor = Color(0xFFFF6D00),
            bg = Color(0xFFFFF3E0)
        )
        InfoBox(
            value = "%.2f".format(distance),
            label = "km",
            icon = Icons.Default.ShowChart,
            iconColor = Color(0xFF2962FF),
            bg = Color(0xFFE3F2FD)
        )
        InfoBox(
            value = "1,000",
            label = "7-day avg",
            icon = Icons.Default.BarChart,
            iconColor = Color(0xFF9C27B0),
            bg = Color(0xFFF3E5F5)
        )
    }
}

@Composable
private fun InfoBox(value: String, label: String, icon: ImageVector, iconColor: Color, bg: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconColor)
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

// -------------------------------
//    DAILY GOAL SELECTOR
// -------------------------------
@Composable
private fun DailyGoal(selectedGoal: String, onGoalSelected: (String) -> Unit) {

    val goals = listOf("5k", "8k", "10k", "12k", "15k")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Daily Goal", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))

        goals.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowItems.forEach { goal ->
                    GoalButton(goal, selectedGoal, onGoalSelected)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun GoalButton(goal: String, selectedGoal: String, onGoalSelected: (String) -> Unit) {
    Button(
        onClick = { onGoalSelected(goal) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (goal == selectedGoal) Color(0xFF00C853) else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (goal != selectedGoal)
            BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
        else null
    ) {
        Text(goal, color = if (goal == selectedGoal) Color.White else Color.Black)
    }
}

// -------------------------------
//    ADD STEPS BUTTON
// -------------------------------
@Composable
private fun AddStepsButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(55.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE8F5E9),
            contentColor = Color(0xFF00C853)
        )
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Add Steps", fontSize = 16.sp)
    }
}

@Composable
private fun RemoveStepsButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(55.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFEBEE),
            contentColor = Color(0xFFD32F2F)
        )
    ) {
        Icon(Icons.Default.Remove, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Remove", fontSize = 16.sp)
    }
}

// -------------------------------
//    TIPS
// -------------------------------
@Composable
private fun TipsToIncreaseSteps() {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFFC107))
                Spacer(Modifier.width(8.dp))
                Text("Tips to Increase Steps", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))
            TipItem("Take the stairs instead of elevators")
            TipItem("Park farther away from entrances")
            TipItem("Take short walking breaks every hour")
            TipItem("Walk while talking on the phone")
        }
    }
}

@Composable
private fun TipItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.Gray, fontSize = 14.sp)
    }
}

// -------------------------------
//    ADD STEPS CHOICE DIALOG
// -------------------------------
@Composable
fun AddStepsChoiceDialog(
    onDismiss: () -> Unit,
    onAddFromGoal: () -> Unit,
    onEnterManually: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(1.dp, Color.Black, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White,
        title = { Text("Add Steps") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("How would you like to add steps?")
                TextButton(
                    onClick = onAddFromGoal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add From Daily Goal")
                }
                TextButton(
                    onClick = onEnterManually,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enter Manually")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// -------------------------------
//    MANUAL ENTRY DIALOG
// -------------------------------
@Composable
fun ManualStepsDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int) -> Unit,
    title: String = "Add Steps Manually",
    submitButtonText: String = "Add",
    showHonestyMessage: Boolean
) {
    var input by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(1.dp, Color.Black, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White,
        confirmButton = {
            TextButton(onClick = {
                val value = input.toIntOrNull() ?: 0
                if (value > 0) onSubmit(value)
            }) {
                Text(submitButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text(title) },
        text = {
            Column {
                if (showHonestyMessage) {
                    Text("Stay honest—entering fake steps only misleads you, not the app.")
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Enter steps") }
                )
            }
        }
    )
}
