package com.simats.nutrisoul

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NutritionInsightsViewModel : ViewModel() {

    private val _insightsData = MutableStateFlow(NutritionInsightsData())
    val insightsData: StateFlow<NutritionInsightsData> = _insightsData

}

data class NutritionInsightsData(
    val weeklyConsistency: Float = 0.14f,
    val daysLogged: Int = 1,
    val averageCalories: Int = 41,
    val targetCalories: Int = 1405,
    val averageProtein: Int = 1,
    val averageCarbs: Int = 10,
    val averageFats: Int = 0,
    val proteinPercentage: Int = 10,
    val carbsPercentage: Int = 98,
    val fatsPercentage: Int = 0
)
