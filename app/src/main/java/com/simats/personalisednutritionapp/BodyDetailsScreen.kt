package com.simats.personalisednutritionapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.simats.personalisednutritionapp.data.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyDetailsScreen(navController: NavHostController, userViewModel: UserViewModel) {

    var height by remember { mutableStateOf("161") }
    var weight by remember { mutableStateOf("65") }
    var heightUnit by remember { mutableStateOf("cm") }
    var weightUnit by remember { mutableStateOf("kg") }

    val bmi = calculateBmi(height, weight, heightUnit, weightUnit)
    val bmiCategory = getBmiCategory(bmi)
    val bmiProgress = (bmi.toFloat() / 40f).coerceIn(0f, 1f)
    val bmiColor = getBmiColor(bmiCategory)


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
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
                modifier = Modifier
                    .fillMaxWidth(),
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

            Icon(
                imageVector = Icons.Default.Balance,
                contentDescription = "Body Details",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))


            Text(
                "Body Details",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Help us calculate your nutrition needs",
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Step Indicator (Step 2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(6) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                if (index <= 1) Color.White else Color.White.copy(alpha = 0.4f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        Card(
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                /* ---------------- HEIGHT ---------------- */
                Text("Height", fontWeight = FontWeight.SemiBold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    MyOutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter height") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    UnitSelector(
                        units = listOf("cm", "ft"),
                        selectedUnit = heightUnit,
                        onUnitSelected = { heightUnit = it }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                /* ---------------- WEIGHT ---------------- */
                Text("Current Weight", fontWeight = FontWeight.SemiBold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    MyOutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter weight") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    UnitSelector(
                        units = listOf("kg", "lbs"),
                        selectedUnit = weightUnit,
                        onUnitSelected = { weightUnit = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                /* ---------------- BMI CARD ---------------- */
                BmiCard(bmi, bmiCategory, bmiProgress, bmiColor)

                Spacer(modifier = Modifier.height(16.dp))

                /* ---------------- BMR INFO CARD ---------------- */
                BmrInfoCard()


                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val user = userViewModel.user.value.copy(
                            height = height.toDouble(),
                            currentWeight = weight.toDouble()
                        )
                        userViewModel.updateUser(user)
                        navController.navigate(Screen.FoodPreferences.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Continue", color = Color.White)
                }
            }
        }
    }
}

/* ---------------- UNIT SELECTOR ---------------- */

@Composable
fun UnitSelector(units: List<String>, selectedUnit: String, onUnitSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {
        units.forEach { unit ->
            Text(
                text = unit,
                modifier = Modifier
                    .background(if (unit == selectedUnit) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color.Transparent)
                    .clickable { onUnitSelected(unit) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                color = if (unit == selectedUnit) Color.White else Color.Black,
                fontWeight = if (unit == selectedUnit) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}


/* ---------------- BMI CARD ---------------- */

@Composable
fun BmiCard(bmi: Double, category: String, progress: Float, bmiColor: Color) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color(0xFFF3F8FF)),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color(0xFFD3E6FF))

    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Your BMI", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.DarkGray)
                Text(category, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = bmiColor,
                    trackColor = Color(0xFFE5E7EB)
                )
            }
            Text(
                String.format("%.1f", bmi),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = bmiColor
            )
        }
    }
}

@Composable
fun BmrInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = "Info",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                "Your height and weight help us calculate your BMR (Basal Metabolic Rate) and daily calorie requirements.",
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }
    }
}


fun calculateBmi(height: String, weight: String, heightUnit: String, weightUnit: String): Double {
    val heightValue = height.toDoubleOrNull() ?: 0.0
    val weightValue = weight.toDoubleOrNull() ?: 0.0

    if (heightValue == 0.0 || weightValue == 0.0) {
        return 0.0
    }

    val heightInMeters = if (heightUnit == "cm") {
        heightValue / 100
    } else { // ft
        heightValue * 0.3048
    }

    val weightInKg = if (weightUnit == "kg") {
        weightValue
    } else { // lbs
        weightValue * 0.453592
    }

    return weightInKg / (heightInMeters * heightInMeters)
}

fun getBmiCategory(bmi: Double): String {
    return when {
        bmi < 18.5 -> "Underweight"
        bmi < 25 -> "Normal"
        bmi < 30 -> "Overweight"
        else -> "Obese"
    }
}

@Composable
fun getBmiColor(category: String): Color {
    return when (category) {
        "Underweight" -> Color.Blue
        "Normal" -> MaterialTheme.colorScheme.primary
        "Overweight" -> Color(0xFFFFA500) // Orange
        "Obese" -> Color.Red
        else -> Color.Gray
    }
}

@Composable
fun MyOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        keyboardOptions = keyboardOptions,
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.primary
        )
    )
}
