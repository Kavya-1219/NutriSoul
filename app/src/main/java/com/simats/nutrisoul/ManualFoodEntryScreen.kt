package com.simats.nutrisoul

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun ManualFoodEntryScreen(navController: NavController, viewModel: LogFoodViewModel = hiltViewModel()) {
    val foodName by viewModel.foodName.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val caloriesPer100g by viewModel.caloriesPer100g.collectAsState()
    val proteinPer100g by viewModel.proteinPer100g.collectAsState()
    val carbsPer100g by viewModel.carbsPer100g.collectAsState()
    val fatsPer100g by viewModel.fatsPer100g.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = foodName,
            onValueChange = { viewModel.foodName.value = it },
            label = { Text("Food Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = quantity,
            onValueChange = { viewModel.quantity.value = it },
            label = { Text("Quantity (g)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = caloriesPer100g,
            onValueChange = { viewModel.caloriesPer100g.value = it },
            label = { Text("Calories per 100g") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = proteinPer100g,
            onValueChange = { viewModel.proteinPer100g.value = it },
            label = { Text("Protein per 100g") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = carbsPer100g,
            onValueChange = { viewModel.carbsPer100g.value = it },
            label = { Text("Carbs per 100g") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = fatsPer100g,
            onValueChange = { viewModel.fatsPer100g.value = it },
            label = { Text("Fats per 100g") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.calculateAndLogFood()
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Food")
        }
    }
}
