package com.simats.personalisednutritionapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.personalisednutritionapp.ui.theme.Green

@Composable
fun GoalWeightScreen(navController: NavController) {

    var targetWeight by remember { mutableStateOf("60") }
    val currentWeight = 65.0
    val weightToLose = (currentWeight - (targetWeight.toDoubleOrNull() ?: 0.0)).coerceAtLeast(0.0)

    val timelines = listOf(
        Timeline("6 weeks", "Aggressive pace", "~1 kg/week"),
        Timeline("7 weeks", "Recommended pace", "~0.75 kg/week", recommended = true),
        Timeline("11 weeks", "Gradual pace", "~0.5 kg/week")
    )

    var selectedTimeline by remember { mutableStateOf(timelines.find { it.recommended }) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Green, Color(0xFF81C784))
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Icon(
                imageVector = Icons.Default.TrendingDown,
                contentDescription = "Goal Weight",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Goal Weight",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Set your target weight",
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Current Weight, Target Weight, Weight to lose
                WeightInfoSection(currentWeight, targetWeight, weightToLose) { newTarget ->
                    targetWeight = newTarget
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Estimated Timeline
                EstimatedTimelineCard(selectedTimeline)

                Spacer(modifier = Modifier.height(24.dp))

                // Choose Timeline
                Text("Choose Your Timeline", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                timelines.forEach { timeline ->
                    TimelineOptionCard(timeline, selectedTimeline == timeline) {
                        selectedTimeline = timeline
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Safety Tip
                SafetyTipCard()

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { navController.navigate("healthConditions") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green
                    )
                ) {
                    Text("Continue", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun WeightInfoSection(
    currentWeight: Double,
    targetWeight: String,
    weightToLose: Double,
    onTargetWeightChange: (String) -> Unit
) {
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Current Weight", color = Color.Gray)
                Text("$currentWeight kg", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        MyOutlinedTextField(
            value = targetWeight,
            onValueChange = onTargetWeightChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Target Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = {
                Icon(Icons.Default.TrendingDown, contentDescription = null, tint = Color(0xFF1565C0))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Weight to lose", fontWeight = FontWeight.SemiBold)
                Text("%.1f kg".format(weightToLose), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
            }
        }
    }
}

@Composable
fun EstimatedTimelineCard(selectedTimeline: Timeline?) {
    selectedTimeline?.let {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF6A1B9A), modifier = Modifier.padding(end = 16.dp))
                Column {
                    Text("Estimated Timeline", fontWeight = FontWeight.SemiBold)
                    Text("~${it.duration}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
                    Text("Based on healthy weight loss of ${it.rate}", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun TimelineOptionCard(timeline: Timeline, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = when {
        isSelected && timeline.recommended -> Green
        isSelected -> Color(0xFF1565C0)
        else -> Color.LightGray
    }
    val containerColor = when {
        isSelected && timeline.recommended -> Green.copy(alpha = 0.1f)
        isSelected -> Color(0xFFE3F2FD)
        else -> Color(0xFFF5F5F5)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(timeline.duration, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (timeline.recommended) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Green, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Recommended", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
            Text(timeline.pace, color = Color.Gray, fontSize = 14.sp)
            Text(timeline.rate, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun SafetyTipCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        border = BorderStroke(1.dp, Color(0xFFFFD54F))
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.padding(end = 8.dp))
            Text(
                "Healthy weight loss is 0.5-1 kg per week. Rapid changes can be unhealthy. Consult a doctor for personalized advice.",
                fontSize = 13.sp,
                color = Color(0xFFF57F17)
            )
        }
    }
}

data class Timeline(val duration: String, val pace: String, val rate: String, val recommended: Boolean = false)
