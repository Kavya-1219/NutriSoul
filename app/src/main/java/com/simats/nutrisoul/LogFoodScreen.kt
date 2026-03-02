package com.simats.nutrisoul

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.nutrisoul.ui.FoodItemUi

@Composable
fun LogFoodScreen(navController: NavController, viewModel: LogFoodViewModel = hiltViewModel()) {
    var selectedFood by remember { mutableStateOf<FoodItemUi?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Header()
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TodayCaloriesCard(viewModel = viewModel)
            Actions(
                onScanClick = { navController.navigate(Screen.ScanFood.route) },
                onManualEntryClick = { navController.navigate(Screen.ManualFoodEntry.route) }
            )
            SearchFoodDatabase(viewModel = viewModel, onFoodSelected = { selectedFood = it })
        }

        selectedFood?.let {
            FoodDetailsSheet(foodItem = it, onLogFood = {
                    item, quantity ->
                viewModel.addFood(item, quantity)
                selectedFood = null
            }, onDismiss = { selectedFood = null })
        }
    }
}

@Composable
private fun Header() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Log Food", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Track your meals with AI", fontSize = 14.sp)
    }
}

@Composable
private fun TodayCaloriesCard(viewModel: LogFoodViewModel) {
    val todayTotals by viewModel.todayTotals.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Today's Calories", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text("${todayTotals.calories.toInt()}/2000", color = Color(0xFFFF5722), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            val progress = if (2000 > 0) (todayTotals.calories / 2000).toFloat() else 0f
            val animatedProgress by animateFloatAsState(targetValue = progress, label = "")
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFFF5722),
                trackColor = Color.LightGray.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                MacroItem(name = "Protein", value = "${todayTotals.protein.toInt()}g")
                MacroItem(name = "Carbs", value = "${todayTotals.carbs.toInt()}g")
                MacroItem(name = "Fats", value = "${todayTotals.fats.toInt()}g")
            }
        }
    }
}

@Composable
private fun MacroItem(name: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(name, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun Actions(onScanClick: () -> Unit, onManualEntryClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ActionButton(
            modifier = Modifier.weight(1f),
            text = "Scan Food",
            subtext = "AI Detection",
            icon = Icons.Default.CameraAlt,
            color = Color(0xFF8E44AD),
            onClick = onScanClick
        )
        ActionButton(
            modifier = Modifier.weight(1f),
            text = "Manual Entry",
            subtext = "Add Manually",
            icon = Icons.Default.Add,
            color = Color(0xFF27AE60),
            onClick = onManualEntryClick
        )
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    subtext: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = text, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtext, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun SearchFoodDatabase(viewModel: LogFoodViewModel, onFoodSelected: (FoodItemUi) -> Unit) {
    val searchResults by viewModel.searchResults.collectAsState()
    val query by viewModel.query.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Search Food Database", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search for food...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.height(200.dp)) {
                items(searchResults) { foodItem ->
                    FoodItemRow(foodItem = foodItem, onClick = { onFoodSelected(foodItem) })
                }
            }
        }
    }
}

@Composable
fun FoodItemRow(foodItem: FoodItemUi, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(foodItem.name, fontWeight = FontWeight.Bold)
            Text("${foodItem.servingQuantity}${foodItem.servingUnit}", color = Color.Gray)
        }
        Text("${foodItem.calories.toInt()} kcal", color = Color(0xFFFF5722), fontWeight = FontWeight.Bold)
        IconButton(onClick = onClick) {
            Icon(Icons.Default.Add, contentDescription = "Log Food")
        }
    }
}

@Composable
fun FoodDetailsSheet(foodItem: FoodItemUi, onLogFood: (FoodItemUi, Double) -> Unit, onDismiss: () -> Unit) {
    var quantity by remember { mutableStateOf(foodItem.servingQuantity.toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(foodItem.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        val quantityValue = quantity.toDoubleOrNull() ?: 0.0
        val servingQuantity = if (foodItem.servingQuantity > 0) foodItem.servingQuantity else 1.0
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            NutritionItem("Calories", "${(foodItem.calories * quantityValue / servingQuantity).toInt()}")
            NutritionItem("Protein", "${String.format("%.1f", foodItem.protein * quantityValue / servingQuantity)}g")
            NutritionItem("Carbs", "${String.format("%.1f", foodItem.carbs * quantityValue / servingQuantity)}g")
            NutritionItem("Fats", "${String.format("%.1f", foodItem.fat * quantityValue / servingQuantity)}g")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            IconButton(onClick = {
                val currentQuantity = quantity.toDoubleOrNull() ?: 0.0
                quantity = (currentQuantity - 1).coerceAtLeast(0.0).toString()
            }) {
                Icon(Icons.Default.Remove, contentDescription = "Remove")
            }
            Text(quantity, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 16.dp))
            IconButton(onClick = {
                val currentQuantity = quantity.toDoubleOrNull() ?: 0.0
                quantity = (currentQuantity + 1).toString()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val quantityValue = quantity.toDoubleOrNull()
                if (quantityValue != null) {
                    onLogFood(foodItem, quantityValue)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
        ) {
            Text("Log Food", color = Color.White)
        }
    }
}

@Composable
fun NutritionItem(name: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(name, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
