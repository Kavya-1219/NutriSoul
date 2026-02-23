package com.simats.nutrisoul.ui

data class FoodItemUi(
    val id: Long,
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val servingQuantity: Double,
    val servingUnit: String
)
