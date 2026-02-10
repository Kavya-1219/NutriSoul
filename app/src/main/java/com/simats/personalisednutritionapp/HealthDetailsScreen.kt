package com.simats.personalisednutritionapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SuggestionChip
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
fun HealthDetailsScreen(navController: NavController, conditionNames: String?) {
    val conditions = remember { conditionNames?.split(',') ?: emptyList() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Green, Color(0xFF81C784))
                )
            )
    ) {
        // Top bar
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
            Text(
                "Your Health Details",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Please provide more details.",
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
            ) {
                Column(modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                ) {
                    if (conditions.contains("High Blood Pressure") || conditions.contains("Low Blood Pressure")) {
                        BloodPressureCard()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (conditions.contains("Diabetes")) {
                        DiabetesCard()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (conditions.contains("Thyroid Issues")) {
                        ThyroidCard()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (conditions.contains("High Cholesterol")) {
                        CholesterolCard()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (conditions.contains("Food Allergies")) {
                        FoodAllergiesCard()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Button(
                    onClick = {
                        navController.navigate("mealPerDay")
                    },
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
fun BloodPressureCard() {
    var systolic by remember { mutableStateOf("") }
    var diastolic by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("❤️ Blood Pressure Reading", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Systolic (top)")
                    MyOutlinedTextField(
                        value = systolic,
                        onValueChange = { systolic = it },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Diastolic (bottom)")
                    MyOutlinedTextField(
                        value = diastolic,
                        onValueChange = { diastolic = it },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Normal: <120/80 | High: ≥140/90 | Low: <90/60", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiabetesCard() {
    var selectedType by remember { mutableStateOf<String?>(null) }
    val types = listOf("Type 1", "Type 2", "Prediabetes")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🩺 Diabetes Type", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                types.forEach { type ->
                    Card(
                        modifier = Modifier.clickable { selectedType = type },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (selectedType == type) Green else Color.LightGray),
                        colors = CardDefaults.cardColors(containerColor = if (selectedType == type) Green.copy(alpha = 0.1f) else Color.White)
                    ) {
                        Text(type, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ThyroidCard() {
    var selectedCondition by remember { mutableStateOf<String?>(null) }
    val conditions = listOf(
        "Hypothyroidism (Underactive)",
        "Hyperthyroidism (Overactive)",
        "Hashimoto’s",
        "Not sure"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🦋 Thyroid Condition", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            conditions.forEach { condition ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCondition = condition },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (selectedCondition == condition) Green else Color.LightGray),
                    colors = CardDefaults.cardColors(containerColor = if (selectedCondition == condition) Green.copy(alpha = 0.1f) else Color.White)
                ) {
                    Text(condition, modifier = Modifier.padding(16.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CholesterolCard() {
    var cholesterolLevel by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🫀 Cholesterol Level (mg/dL)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            MyOutlinedTextField(
                value = cholesterolLevel,
                onValueChange = { cholesterolLevel = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., 220") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Normal: <200 | High: ≥240 mg/dL", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodAllergiesCard() {
    val allergies = listOf("Peanuts", "Tree nuts", "Milk/Dairy", "Eggs", "Soy", "Wheat/Gluten", "Shellfish", "Fish", "Sesame")
    var selectedAllergies by remember { mutableStateOf(setOf<String>()) }
    var otherAllergies by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("⚠️ Select Your Food Allergies", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                allergies.forEach { allergy ->
                    val isSelected = selectedAllergies.contains(allergy)
                    Card(
                        modifier = Modifier.clickable {
                            selectedAllergies = if (isSelected) {
                                selectedAllergies - allergy
                            } else {
                                selectedAllergies + allergy
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (isSelected) Green else Color.LightGray),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) Green.copy(alpha = 0.1f) else Color.White)
                    ) {
                        Text(allergy, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Other allergies (optional)")
            MyOutlinedTextField(
                value = otherAllergies,
                onValueChange = { otherAllergies = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Strawberries, Mustard") }
            )
        }
    }
}
