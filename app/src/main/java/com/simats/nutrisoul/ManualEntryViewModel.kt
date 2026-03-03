package com.simats.nutrisoul

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.FoodRepository
import com.simats.nutrisoul.data.models.FoodLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ManualEntryViewModel @Inject constructor(
    private val repository: FoodRepository
) : ViewModel() {
    val foodName = MutableStateFlow("")
    val quantity = MutableStateFlow("")
    val caloriesPer100g = MutableStateFlow("")
    val proteinPer100g = MutableStateFlow("")
    val carbsPer100g = MutableStateFlow("")
    val fatsPer100g = MutableStateFlow("")

    fun calculateAndLogFood() {
        viewModelScope.launch {
            val qty = quantity.value.toDoubleOrNull() ?: 0.0
            val factor = qty / 100.0
            val foodLog = FoodLog(
                name = foodName.value,
                calories = (caloriesPer100g.value.toDoubleOrNull() ?: 0.0) * factor,
                protein = (proteinPer100g.value.toDoubleOrNull() ?: 0.0) * factor,
                carbs = (carbsPer100g.value.toDoubleOrNull() ?: 0.0) * factor,
                fats = (fatsPer100g.value.toDoubleOrNull() ?: 0.0) * factor,
                quantity = qty,
                mealType = "Manual Entry",
                date = LocalDate.now()
            )
            repository.addFoodToDailyIntake(foodLog)
        }
    }
}
