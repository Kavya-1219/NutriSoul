package com.simats.nutrisoul

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AiFoodScannerModal(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan Food with AI") },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Gray.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Upload Image", tint = Color.Gray)
                    Text("Upload Food Image", color = Color.Gray)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun ManualFoodEntryModal(onDismiss: () -> Unit, onLogFood: () -> Unit) {
    var foodName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("g") }
    var showNutrition by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Manual Food Entry", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it; showNutrition = false },
                    label = { Text("Food Name") },
                    placeholder = { Text("e.g., Grilled Chicken") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it; showNutrition = false },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it; showNutrition = false },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(16.dp))
                 Button(
                        onClick = { showNutrition = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = foodName.isNotBlank() && quantity.isNotBlank() && unit.isNotBlank(),
                         colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F39F6))
                    ) {
                        Text("Calculate Nutrition", color = Color.White)
                    }

                AnimatedVisibility(visible = showNutrition) {
                    Column {
                        Spacer(Modifier.height(24.dp))
                        NutritionInfo(foodName.uppercase(), "$quantity $unit", onLogFood)
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun NutritionInfo(
    foodName: String,
    quantity: String,
    onLogFood: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(foodName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(quantity)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                NutritionDetail("Calories", "150")
                NutritionDetail("Protein", "23g")
                NutritionDetail("Carbs", "60g")
                NutritionDetail("Fats", "30g")
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onLogFood,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
            ) {
                Icon(Icons.Default.Check, contentDescription = "Log Food")
                Spacer(Modifier.width(8.dp))
                Text("Log Food")
            }
        }
    }
}


@Composable
fun QuantitySelectorModal(onDismiss: () -> Unit, onLogFood: () -> Unit) {
    var count by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Banana", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    NutritionDetail("Calories", "105")
                    NutritionDetail("Protein", "1.3g")
                    NutritionDetail("Carbs", "27.0g")
                    NutritionDetail("Fats", "0.3g")
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { if (count > 1) count-- }) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease count")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text("${count}x", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("1 medium (118g)", color = Color.Gray)
                    }
                    IconButton(onClick = { count++ }) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase count")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onLogFood,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00))
            ) {
                Icon(Icons.Default.Check, contentDescription = "Log Food")
                Spacer(Modifier.width(8.dp))
                Text("Log Food")
            }
        }
    )
}
