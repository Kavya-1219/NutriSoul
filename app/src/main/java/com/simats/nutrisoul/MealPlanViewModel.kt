package com.simats.nutrisoul

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.meal.MealPlanGenerator
import com.simats.nutrisoul.data.meal.UserNutritionProfile
import com.simats.nutrisoul.data.meal.model.Meal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MealPlanViewModel @Inject constructor() : ViewModel() {

    private val generator = MealPlanGenerator()

    private val _uiState = MutableStateFlow<MealPlanUiState>(MealPlanUiState.Loading)
    val uiState: StateFlow<MealPlanUiState> = _uiState

    // TEMP profile until backend connects
    private val profile = UserNutritionProfile(
        goal = "maintain",
        dietType = "vegetarian",
        allergies = emptyList(),
        healthConditions = emptyList(),
        targetCalories = 2258
    )

    private val userKey = "local_user"

    init {
        loadToday()
    }

    fun loadToday() = viewModelScope.launch {
        _uiState.value = MealPlanUiState.Loading
        try {
            val plan = generator.generate(profile, seed = generator.todaySeed(userKey))
            _uiState.value = MealPlanUiState.Ready(plan)
        } catch (e: Exception) {
            _uiState.value = MealPlanUiState.Error(e.message ?: "Failed to load meal plan")
        }
    }

    fun refresh() = viewModelScope.launch {
        try {
            val plan = generator.generate(profile, seed = System.currentTimeMillis().toString())
            _uiState.value = MealPlanUiState.Ready(plan)
        } catch (e: Exception) {
            _uiState.value = MealPlanUiState.Error(e.message ?: "Refresh failed")
        }
    }

    fun getAlternatives(mealType: String): List<Meal> =
        generator.alternativesFor(mealType, profile)

    fun swapMeal(mealType: String, selected: Meal) = viewModelScope.launch {
        val current = (_uiState.value as? MealPlanUiState.Ready)?.plan ?: return@launch
        val updatedMeals = current.meals.map { if (it.mealType.equals(mealType, true)) selected else it }
        _uiState.value = MealPlanUiState.Ready(current.copy(meals = updatedMeals))
    }

    fun currentProfile(): UserNutritionProfile = profile
}
