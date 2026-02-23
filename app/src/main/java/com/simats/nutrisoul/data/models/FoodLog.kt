package com.simats.nutrisoul.data.models

import com.simats.nutrisoul.data.IntakeEntity
import java.time.LocalDate

data class FoodLog(
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val quantity: Double,
    val mealType: String,
    val date: LocalDate = LocalDate.now()
)

fun FoodLog.toIntakeEntity() = IntakeEntity(
    name = name,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fats = fats,
    quantity = quantity,
    mealType = mealType,
    date = date
)

fun IntakeEntity.toFoodLog() = FoodLog(
    name = name,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fats = fats,
    quantity = quantity,
    mealType = mealType,
    date = date
)
