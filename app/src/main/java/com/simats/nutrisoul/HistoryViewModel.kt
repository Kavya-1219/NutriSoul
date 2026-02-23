package com.simats.nutrisoul

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HistoryViewModel : ViewModel() {

    private val _historyData = MutableStateFlow(HistoryData())
    val historyData: StateFlow<HistoryData> = _historyData

}

data class HistoryData(
    val daysLogged: Int = 1,
    val totalMeals: Int = 1,
    val loggedFoods: List<LoggedFood> = listOf(
        LoggedFood(
            name = "Carrot",
            quantity = "1x medium",
            calories = 41,
            time = "3:50 PM",
            protein = 1,
            carbs = 10,
            fats = 0
        )
    )
)

data class LoggedFood(
    val name: String,
    val quantity: String,
    val calories: Int,
    val time: String,
    val protein: Int,
    val carbs: Int,
    val fats: Int
)
