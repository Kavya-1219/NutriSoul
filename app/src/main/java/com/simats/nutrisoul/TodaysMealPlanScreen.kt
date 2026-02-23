package com.simats.nutrisoul

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.simats.nutrisoul.data.meal.model.Ingredient
import com.simats.nutrisoul.data.meal.model.Meal
import com.simats.nutrisoul.data.meal.model.MealPlan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodaysMealPlanScreen(navController: NavController, viewModel: MealPlanViewModel = hiltViewModel()) {
    val mealPlan by viewModel.mealPlan.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Meal Plan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        content = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(16.dp)
            ) {
                item {
                    Header(mealPlan)
                    Spacer(modifier = Modifier.height(16.dp))
                    PersonalizedForYourGoals()
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(mealPlan.meals) { meal ->
                    MealCard(meal)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    )
}

@Composable
private fun Header(mealPlan: MealPlan) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFF2E7D32), shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("Today's Meal Plan", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Total: ${mealPlan.totalCalories} kcal", color = Color.White, fontSize = 16.sp)
        Text("Protein: ${mealPlan.totalProtein}g • Carbs: ${mealPlan.totalCarbs}g • Fats: ${mealPlan.totalFats}g", color = Color.White, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Target: ${mealPlan.targetCalories} kcal", color = Color.White, fontSize = 16.sp)
            Text("${mealPlan.totalCalories - mealPlan.targetCalories} kcal", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PersonalizedForYourGoals() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF1976D2))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text("Personalized for Your Goals", color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
            Text("This plan is customized for maintain weight and your vegetarian diet.", fontSize = 12.sp)
        }
    }
}

@Composable
private fun MealCard(meal: Meal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(meal.icon, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(meal.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(meal.mealName, fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MacroChip("Calories", meal.calories.toString(), Color(0xFFE8F5E9), Color(0xFF388E3C))
                MacroChip("Protein", "${meal.protein}g", Color(0xFFE3F2FD), Color(0xFF1976D2))
                MacroChip("Carbs", "${meal.carbs}g", Color(0xFFFFF3E0), Color(0xFFFFA000))
                MacroChip("Fats", "${meal.fats}g", Color(0xFFFBE9E7), Color(0xFFD84315))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("ITEMS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            meal.ingredients.forEach { ingredient ->
                IngredientRow(ingredient)
            }
        }
    }
}

@Composable
private fun MacroChip(name: String, value: String, backgroundColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(name, fontSize = 12.sp, color = textColor)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
private fun IngredientRow(ingredient: Ingredient) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(ingredient.name, fontWeight = FontWeight.Bold)
            Text(ingredient.quantity, color = Color.Gray, fontSize = 12.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${ingredient.calories} kcal", fontWeight = FontWeight.Bold)
            Text("P:${ingredient.protein}g C:${ingredient.carbs}g F:${ingredient.fats}g", color = Color.Gray, fontSize = 10.sp)
        }
    }
}
