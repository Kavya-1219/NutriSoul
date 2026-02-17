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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.personalisednutritionapp.data.UserViewModel
import com.simats.personalisednutritionapp.ui.theme.PrimaryGreen
import kotlin.math.abs

@Composable
fun GoalWeightScreen(navController: NavController, userViewModel: UserViewModel) {

    val currentWeight = userViewModel.currentWeight.toInt()
    var targetWeight by remember { mutableStateOf("") }
    val goalType = userViewModel.goalType

    val target = targetWeight.toIntOrNull()
    val weightDifference =
        when (goalType) {
            "Lose weight" -> if (target != null) currentWeight - target else 0
            "Gain weight" -> if (target != null) target - currentWeight else 0
            else -> 0
        }

    val estimatedWeeks =
        if (weightDifference > 0) (abs(weightDifference) / 0.75).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGreen)
    ) {

        HeaderSection(navController)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color(0xFFF7F7F7))
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                WeightInfoSection(
                    currentWeight = currentWeight,
                    targetWeight = targetWeight,
                    onTargetWeightChange = { targetWeight = it },
                    showDifference = (goalType == "Lose weight" || goalType == "Gain weight"),
                    goalType = goalType,
                    weightDifference = weightDifference
                )

                if (goalType == "Lose weight" || goalType == "Gain weight") {
                    Spacer(modifier = Modifier.height(24.dp))
                    EstimatedTimelineCard(estimatedWeeks)
                    Spacer(modifier = Modifier.height(32.dp))
                    TimelineSection()
                    Spacer(modifier = Modifier.height(24.dp))
                    SafetyTipCard()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            ContinueButton(
                modifier = Modifier.padding(24.dp),
                enabled = targetWeight.isNotEmpty(),
                onClick = {
                    userViewModel.updateTargetWeight(target?.toDouble() ?: 0.0)
                    navController.navigate(Screen.HealthConditions.route)
                }
            )
        }
    }
}

@Composable
fun HeaderSection(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(painterResource(id = R.drawable.ic_target), contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Goal Weight", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Set your target weight", color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp)
        }
    }
}


@Composable
fun WeightInfoSection(
    currentWeight: Int,
    targetWeight: String,
    onTargetWeightChange: (String) -> Unit,
    showDifference: Boolean,
    goalType: String,
    weightDifference: Int
) {
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF90CAF9))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Current Weight", color = Color.Gray, fontSize = 16.sp)
                Text("$currentWeight kg", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = targetWeight,
            onValueChange = onTargetWeightChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Target Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                focusedLabelColor = PrimaryGreen,
                cursorColor = PrimaryGreen,
                focusedTextColor = PrimaryGreen
            ),
            trailingIcon = {
                Icon(Icons.Default.TrendingDown, contentDescription = null, tint = Color(0xFF1565C0))
            }
        )

        if (showDifference && weightDifference > 0) {
            Spacer(modifier = Modifier.height(20.dp))

            val label = if (goalType == "Lose weight") "Weight to lose" else "Weight to gain"

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF90CAF9))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(
                        "$weightDifference.0 kg",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                }
            }
        }
    }
}

data class TimelineInfo(
    val duration: String,
    val pace: String,
    val rate: String,
    val recommended: Boolean,
    val containerColor: Color,
    val borderColor: Color
)

@Composable
fun TimelineSection() {
    Text("Choose Your Timeline", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(16.dp))

    var selectedTimeline by remember { mutableStateOf("7 weeks") }

    val timelines = listOf(
        TimelineInfo("6 weeks", "Aggressive pace", "~1 kg/week", false, Color(0xFFFFF3E0), Color(0xFFFB8C00)),
        TimelineInfo("7 weeks", "Recommended pace", "~0.75 kg/week", true, Color(0xFFE8F5E9), PrimaryGreen),
        TimelineInfo("11 weeks", "Gradual pace", "~0.5 kg/week", false, Color(0xFFE3F2FD), Color(0xFF2196F3))
    )

    timelines.forEach { timeline ->
        TimelineOptionCard(
            info = timeline,
            selected = selectedTimeline == timeline.duration,
            onClick = { selectedTimeline = timeline.duration }
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun EstimatedTimelineCard(weeks: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFCE93D8))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF6A1B9A))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Estimated Timeline", fontWeight = FontWeight.Bold, color = Color(0xFF4A148C))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("~$weeks weeks", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
            Text("Based on healthy weight loss of 0.75 kg/week", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun TimelineOptionCard(
    info: TimelineInfo,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = BorderStroke(if (selected) 2.dp else 1.dp, info.borderColor),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = info.containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(info.duration, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (info.recommended) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(PrimaryGreen, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Recommended", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(info.pace, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(info.rate, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SafetyTipCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFFFE082))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.padding(top=2.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Safe weight loss is 0.5–1 kg per week. Rapid changes can be harmful. Consult a doctor for personalized advice.",
                color = Color(0xFFB57511),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun ContinueButton(modifier: Modifier = Modifier, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryGreen,
            disabledContainerColor = Color.LightGray
        )
    ) {
        Text("Continue", color = Color.White, fontSize = 18.sp)
    }
}
