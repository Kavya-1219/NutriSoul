package com.simats.nutrisoul.data.models

import com.simats.nutrisoul.data.network.ApiFoodItem

fun ApiFoodItem.toFoodItem(): FoodItem {
    return FoodItem(
        name = description,
        calories = foodNutrients.find { it.nutrientName == "Energy" }?.value?.toDouble() ?: 0.0,
        protein = foodNutrients.find { it.nutrientName == "Protein" }?.value?.toDouble() ?: 0.0,
        carbs = foodNutrients.find { it.nutrientName == "Carbohydrate, by difference" }?.value?.toDouble() ?: 0.0,
        fats = foodNutrients.find { it.nutrientName == "Total lipid (fat)" }?.value?.toDouble() ?: 0.0
    )
}
