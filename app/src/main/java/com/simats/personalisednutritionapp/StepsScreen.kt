package com.simats.personalisednutritionapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StepsScreen(navController: NavController) {
    var selectedGoal by remember { mutableStateOf("5k") }

    val goalInSteps = selectedGoal.replace("k", "000").toIntOrNull() ?: 5000

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00C853))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            StepsHeader(navController)
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
                StepsProgress(UserData.steps.value, goalInSteps)
                TrackingModeSwitch(UserData.automaticTracking.value) { UserData.automaticTracking.value = it }
                MotivationMessage()
                InfoBoxes(UserData.steps.value)
                DailyGoal(selectedGoal) { selectedGoal = it }
                AddStepsButton { UserData.steps.value += 1000 } // Placeholder for manual step entry
                TipsToIncreaseSteps()
            }
        }
    }
}

@Composable
private fun StepsHeader(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = "Steps Tracking",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Every step brings you closer!",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun StepsProgress(steps: Int, goalInSteps: Int) {
    val progress = if (goalInSteps > 0) steps.toFloat() / goalInSteps else 0f
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
            trackColor = Color(0xFFE0E0E0).copy(alpha = 0.5f)
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

@Composable
private fun TrackingModeSwitch(automaticTracking: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Automatic Tracking")
            Switch(
                checked = automaticTracking,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

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

@Composable
private fun InfoBoxes(steps: Int) {
    val calories = (steps * 0.05).toInt()
    val distanceInKm = steps * 0.0008

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        InfoBox(
            value = calories.toString(),
            label = "kcal",
            icon = Icons.Default.LocalFireDepartment,
            iconColor = Color(0xFFFF6D00),
            backgroundColor = Color(0xFFE8F5E9).copy(alpha=0.5f)
        )
        InfoBox(
            value = "%.2f".format(distanceInKm),
            label = "km",
            icon = Icons.Default.ShowChart,
            iconColor = Color(0xFF2962FF),
             backgroundColor = Color(0xFFE3F2FD).copy(alpha=0.5f)
        )
        InfoBox(
            value = "1,000", // This would ideally be calculated based on historical data
            label = "7-day avg",
            icon = Icons.Default.BarChart,
            iconColor = Color(0xFF9C27B0),
            backgroundColor = Color(0xFFF3E5F5).copy(alpha=0.5f)
        )
    }
}

@Composable
private fun InfoBox(
    value: String,
    label: String,
    icon: ImageVector,
    iconColor: Color,
    backgroundColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
private fun DailyGoal(selectedGoal: String, onGoalSelected: (String) -> Unit) {
    val goals = listOf("5k", "8k", "10k", "12k", "15k")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Daily Goal", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            goals.take(3).forEach { goal ->
                GoalButton(goal, selectedGoal, onGoalSelected)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            goals.takeLast(2).forEach { goal ->
                 GoalButton(goal, selectedGoal, onGoalSelected)
            }
        }
    }
}

@Composable
private fun GoalButton(goal: String, selectedGoal: String, onGoalSelected: (String) -> Unit) {
    Button(
        onClick = { onGoalSelected(goal) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selectedGoal == goal) Color(0xFF00C853) else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = if (selectedGoal != goal) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null,
        border = if (selectedGoal != goal) BorderStroke(1.dp, Color.LightGray.copy(alpha=0.5f)) else null

    ) {
        Text(goal, color = if (selectedGoal == goal) Color.White else Color.Black)
    }
}


@Composable
private fun AddStepsButton(onAddSteps: () -> Unit) {
    Button(
        onClick = onAddSteps,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Add Steps", fontSize = 16.sp)
    }
}

@Composable
private fun TipsToIncreaseSteps() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
         border = BorderStroke(1.dp, Color.LightGray.copy(alpha=0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFFC107))
                Spacer(Modifier.width(8.dp))
                Text("Tips to Increase Steps", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.Gray, fontSize = 14.sp)
    }
}
