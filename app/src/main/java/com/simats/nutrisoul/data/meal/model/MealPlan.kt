package com.simats.nutrisoul.data.meal.model

data class MealPlan(
    val totalCalories: Int,
    val totalProtein: Int,
    val totalCarbs: Int,
    val totalFats: Int,
    val targetCalories: Int,
    val meals: List<Meal>
)
