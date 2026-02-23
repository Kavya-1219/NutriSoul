package com.simats.nutrisoul

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LunchDining
import androidx.lifecycle.ViewModel
import com.simats.nutrisoul.data.meal.model.Ingredient
import com.simats.nutrisoul.data.meal.model.Meal
import com.simats.nutrisoul.data.meal.model.MealPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MealPlanViewModel @Inject constructor() : ViewModel() {

    private val _mealPlan = MutableStateFlow(
        MealPlan(
            totalCalories = 1135,
            totalProtein = 58,
            totalCarbs = 132,
            totalFats = 43,
            targetCalories = 2258,
            meals = listOf(
                Meal(
                    name = "Breakfast",
                    mealName = "Paneer Sandwich with Veggies",
                    icon = Icons.Default.BreakfastDining,
                    calories = 323,
                    protein = 16,
                    carbs = 31,
                    fats = 16,
                    ingredients = listOf(
                        Ingredient(
                            name = "Whole Wheat Bread",
                            quantity = "2 slices",
                            calories = 140,
                            protein = 6,
                            carbs = 26,
                            fats = 2
                        ),
                        Ingredient(
                            name = "Paneer",
                            quantity = "50g",
                            calories = 132,
                            protein = 9,
                            carbs = 2,
                            fats = 10
                        ),
                        Ingredient(
                            name = "Vegetables (tomato, cucumber)",
                            quantity = "50g",
                            calories = 15,
                            protein = 1,
                            carbs = 3,
                            fats = 0
                        ),
                        Ingredient(
                            name = "Butter",
                            quantity = "5g",
                            calories = 36,
                            protein = 0,
                            carbs = 0,
                            fats = 4
                        )
                    )
                ),
                Meal(
                    name = "Lunch",
                    mealName = "Paneer Curry with Quinoa",
                    icon = Icons.Default.LunchDining,
                    calories = 457,
                    protein = 25,
                    carbs = 42,
                    fats = 21,
                    ingredients = listOf(
                        Ingredient(
                            name = "Quinoa",
                            quantity = "150g cooked",
                            calories = 167,
                            protein = 6,
                            carbs = 29,
                            fats = 3
                        ),
                        Ingredient(
                            name = "Paneer Curry",
                            quantity = "150g",
                            calories = 265,
                            protein = 18,
                            carbs = 8,
                            fats = 18
                        ),
                        Ingredient(
                            name = "Salad",
                            quantity = "100g",
                            calories = 25,
                            protein = 1,
                            carbs = 5,
                            fats = 0
                        )
                    )
                ),
                Meal(
                    name = "Dinner",
                    mealName = "Roti with Palak Paneer",
                    icon = Icons.Default.DinnerDining,
                    calories = 382,
                    protein = 22,
                    carbs = 43,
                    fats = 16,
                    ingredients = listOf(
                        Ingredient(
                            name = "Roti",
                            quantity = "2 medium",
                            calories = 142,
                            protein = 6,
                            carbs = 30,
                            fats = 1
                        ),
                        Ingredient(
                            name = "Palak Paneer",
                            quantity = "150g",
                            calories = 180,
                            protein = 12,
                            carbs = 8,
                            fats = 12
                        ),
                        Ingredient(
                            name = "Curd",
                            quantity = "100g",
                            calories = 60,
                            protein = 4,
                            carbs = 6,
                            fats = 2
                        )
                    )
                ),
                Meal(
                    name = "Snack",
                    mealName = "Sprouts Salad",
                    icon = Icons.Default.Fastfood,
                    calories = 95,
                    protein = 8,
                    carbs = 16,
                    fats = 1,
                    ingredients = listOf(
                        Ingredient(
                            name = "Mixed Sprouts",
                            quantity = "100g",
                            calories = 90,
                            protein = 8,
                            carbs = 15,
                            fats = 1
                        ),
                        Ingredient(
                            name = "Lemon & Spices",
                            quantity = "10g",
                            calories = 5,
                            protein = 0,
                            carbs = 1,
                            fats = 0
                        )
                    )
                )
            )
        )
    )
    val mealPlan: StateFlow<MealPlan> = _mealPlan
}
