package com.simats.nutrisoul.data

data class FoodLog(
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val quantity: Double,
    val mealType: String
)
