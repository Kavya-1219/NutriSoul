package com.simats.nutrisoul.data.meal.model

import androidx.compose.ui.graphics.vector.ImageVector

data class Meal(
    val name: String,
    val mealName: String,
    val icon: ImageVector,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val ingredients: List<Ingredient>
)
