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
            calories = nutrients["Energy"] ?: 0.0,
            protein = nutrients["Protein"] ?: 0.0,
            carbs = nutrients["Carbohydrate, by difference"] ?: 0.0,
            fats = nutrients["Total lipid (fat)"] ?: 0.0
        )
    }
}

fun FoodLog.toEntity(): IntakeEntity {
    return IntakeEntity(
        name = name,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fats = fats,
        mealType = mealType,
        date = date,
        quantity = 1.0 // Defaulting quantity to 1.0, you can change this as needed
    )
}

fun FoodItem.toEntity(): CustomFoodEntity {
    return CustomFoodEntity(
        name = name,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fats = fats
    )
}
