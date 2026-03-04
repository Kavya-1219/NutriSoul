package com.simats.nutrisoul.data

import com.simats.nutrisoul.data.CustomFoodEntity
import com.simats.nutrisoul.data.IntakeEntity
import com.simats.nutrisoul.data.models.FoodItem
import com.simats.nutrisoul.data.models.FoodLog
import com.simats.nutrisoul.data.network.FoodSearchResponse

fun FoodSearchResponse.toFoodItems(): List<FoodItem> {
    return foods.map { apiFoodItem ->
        val nutrients = apiFoodItem.foodNutrients.associate { it.nutrientName to it.value }
        FoodItem(
            name = apiFoodItem.description,
            caloriesPer100g = nutrients["Energy"] ?: 0.0,
            proteinPer100g = nutrients["Protein"] ?: 0.0,
            carbsPer100g = nutrients["Carbohydrate, by difference"] ?: 0.0,
            fatsPer100g = nutrients["Total lipid (fat)"] ?: 0.0,
            servingQuantity = 100.0,
            servingUnit = "g"
        )
    }
}

fun FoodLog.toEntity(): IntakeEntity {
    return IntakeEntity(
        id = id,
        name = name,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fats = fats,
        mealType = mealType,
        date = date,
        quantity = quantity
    )
}

fun FoodItem.toEntity(): CustomFoodEntity {
    return CustomFoodEntity(
        name = name,
        calories = caloriesPer100g,
        protein = proteinPer100g,
        carbs = carbsPer100g,
        fats = fatsPer100g
    )
}
