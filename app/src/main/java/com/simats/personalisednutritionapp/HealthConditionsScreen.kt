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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.personalisednutritionapp.ui.theme.Green

data class HealthCondition(val name: String, val description: String, val icon: String)

@Composable
fun HealthConditionsScreen(navController: NavController) {

    val healthConditions = listOf(
        HealthCondition("None", "No health conditions", "✅"),
        HealthCondition("Diabetes", "Blood sugar management", "🩺"),
        HealthCondition("PCOS", "Hormonal balance", "💊"),
        HealthCondition("Thyroid Issues", "Thyroid regulation", "🦋"),
        HealthCondition("High Blood Pressure", "Blood pressure control", "❤️"),
        HealthCondition("Low Blood Pressure", "Blood pressure support", "💙"),
        HealthCondition("High Cholesterol", "Cholesterol management", "🫀"),
        HealthCondition("Digestive Issues", "Gut health", "🌿"),
        HealthCondition("Anemia", "Iron deficiency", "🩸"),
        HealthCondition("Food Allergies", "Allergy management", "⚠️")
    )

    var selectedConditions by remember { mutableStateOf(setOf(healthConditions.first())) }

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
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    Icons.Outlined.FavoriteBorder,
                    contentDescription = "Health",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Health Conditions",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Select any conditions you have",
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Step Indicator (Step 6)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(7) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                if (index <= 5) Color.White else Color.White.copy(alpha = 0.4f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
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
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    healthConditions.forEach { condition ->
                        HealthConditionCard(
                            condition = condition,
                            isSelected = selectedConditions.contains(condition),
                            onClick = {
                                val newSelection = selectedConditions.toMutableSet()
                                if (condition.name == "None") {
                                    newSelection.clear()
                                    newSelection.add(condition)
                                } else {
                                    newSelection.removeIf { it.name == "None" }
                                    if (newSelection.contains(condition)) {
                                        newSelection.remove(condition)
                                    } else {
                                        newSelection.add(condition)
                                    }
                                    if (newSelection.isEmpty()) {
                                        newSelection.add(healthConditions.first())
                                    }
                                }
                                selectedConditions = newSelection
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Green.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, Green.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Your food and nutrition recommendations will be customized based on your health conditions.",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        navController.navigate("activityLevel")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green
                    ),
                    enabled = selectedConditions.isNotEmpty()
                ) {
                    Text("Continue", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun HealthConditionCard(condition: HealthCondition, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) Green else Color.LightGray
    val containerColor = if (isSelected) Green.copy(alpha = 0.1f) else Color.White
    val titleColor = if (isSelected) Color.Black else Color.DarkGray
    val subtitleColor = if (isSelected) Color.DarkGray else Color.Gray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = condition.icon, fontSize = 24.sp)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = condition.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = titleColor
                )
                Text(
                    text = condition.description,
                    fontSize = 12.sp,
                    color = subtitleColor
                )
            }


            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Green, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}