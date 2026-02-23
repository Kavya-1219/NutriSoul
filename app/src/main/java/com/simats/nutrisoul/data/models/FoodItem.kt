package com.simats.nutrisoul.data.models

import com.simats.nutrisoul.data.CustomFoodEntity

data class FoodItem(
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double
)

fun FoodItem.toCustomFoodEntity() = CustomFoodEntity(
    name = name,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fats = fats
)

fun CustomFoodEntity.toFoodItem() = FoodItem(
    name = name,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fats = fats
)
