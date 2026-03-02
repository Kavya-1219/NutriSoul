package com.simats.nutrisoul

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.FoodRepository
import com.simats.nutrisoul.data.models.FoodItem
import com.simats.nutrisoul.data.models.FoodLog
import com.simats.nutrisoul.data.models.DailyTotals
import com.simats.nutrisoul.ui.DailyTotalsUi
import com.simats.nutrisoul.ui.FoodItemUi
import com.simats.nutrisoul.ui.UserTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class LogFoodViewModel @Inject constructor(private val repository: FoodRepository) : ViewModel() {

    val todayTotals: StateFlow<DailyTotalsUi> =
        repository.observeTodayTotals()
            .map { totals: DailyTotals ->
                DailyTotalsUi(
                    calories = totals.calories ?: 0.0,
                    protein = totals.protein ?: 0.0,
                    carbs = totals.carbs ?: 0.0,
                    fats = totals.fats ?: 0.0
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
                initialValue = DailyTotalsUi()
            )

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _searchResults = MutableStateFlow<List<FoodItemUi>>(emptyList())
    val searchResults: StateFlow<List<FoodItemUi>> = _searchResults

    private val _calculatedNutrition = MutableStateFlow<FoodLog?>(null)
    val calculatedNutrition: StateFlow<FoodLog?> = _calculatedNutrition

    private val _scanResult = MutableStateFlow<List<FoodItem>>(emptyList())
    val scanResult: StateFlow<List<FoodItem>> = _scanResult

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions

    val foodName = MutableStateFlow("")
    val quantity = MutableStateFlow("")
    val caloriesPer100g = MutableStateFlow("")
    val proteinPer100g = MutableStateFlow("")
    val carbsPer100g = MutableStateFlow("")
    val fatsPer100g = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _query
                .debounce(300)
                .filter { it.length > 2 }
                .flatMapLatest { repository.searchFoods(it, "YOUR_API_KEY") }
                .collect { _searchResults.value = it.map(::toFoodItemUi) }
        }
    }

    fun onQueryChanged(query: String) {
        _query.value = query
    }

    fun addFood(foodItem: FoodItemUi, quantity: Double) {
        viewModelScope.launch {
            val foodLog = FoodLog(
                name = foodItem.name,
                calories = foodItem.calories * quantity / 100,
                protein = foodItem.protein * quantity / 100,
                carbs = foodItem.carbs * quantity / 100,
                fats = foodItem.fat * quantity / 100,
                quantity = quantity,
                mealType = getMealType()
            )
            repository.addFoodToDailyIntake(foodLog)
        }
    }

    fun calculateAndLogFood() {
        viewModelScope.launch {
            val quantityValue = quantity.value.toDoubleOrNull() ?: return@launch
            val caloriesValue = caloriesPer100g.value.toDoubleOrNull() ?: return@launch
            val proteinValue = proteinPer100g.value.toDoubleOrNull() ?: return@launch
            val carbsValue = carbsPer100g.value.toDoubleOrNull() ?: return@launch
            val fatsValue = fatsPer100g.value.toDoubleOrNull() ?: return@launch

            val foodItem = FoodItem(
                name = foodName.value,
                calories = caloriesValue,
                protein = proteinValue,
                carbs = carbsValue,
                fats = fatsValue
            )

            val foodLog = FoodLog(
                name = foodName.value,
                calories = caloriesValue * quantityValue / 100,
                protein = proteinValue * quantityValue / 100,
                carbs = carbsValue * quantityValue / 100,
                fats = fatsValue * quantityValue / 100,
                quantity = quantityValue,
                mealType = "Manual Entry"
            )

            repository.saveCustomFood(foodItem)
            repository.addFoodToDailyIntake(foodLog)
            _calculatedNutrition.value = foodLog
        }
    }

    fun analyzeFoodImage(bitmap: Bitmap) {
        viewModelScope.launch {
            // Placeholder for food analysis
            val detectedFoods = listOf("carrot")

            val foodItems = detectedFoods.mapNotNull { foodName ->
                repository.searchFoods(foodName, "YOUR_API_KEY").first().firstOrNull()
            }
            _scanResult.value = foodItems

            foodItems.forEach { foodItem ->
                val foodLog = FoodLog(
                    name = foodItem.name,
                    calories = foodItem.calories,
                    protein = foodItem.protein,
                    carbs = foodItem.carbs,
                    fats = foodItem.fats,
                    quantity = 100.0, // Default to 100g
                    mealType = "Scan"
                )
                repository.addFoodToDailyIntake(foodLog)
            }

            generateSuggestions()
        }
    }

    private fun generateSuggestions() {
        viewModelScope.launch {
            val totals = repository.observeTodayTotals().first()
            val target = UserTarget() // Assuming a default target

            val suggestions = mutableListOf<String>()

            if ((totals.protein ?: 0.0) < target.protein) {
                suggestions.add("Increase your protein intake.")
            }

            if ((totals.calories ?: 0.0) > target.calories) {
                suggestions.add("You have exceeded your calorie target. Consider reducing high-calorie foods.")
            }

            if ((totals.carbs ?: 0.0) > target.carbs) {
                suggestions.add("Your carb intake is high. Consider reducing carb-heavy items.")
            }

            if (target.calories - (totals.calories ?: 0.0) > 200) {
                suggestions.add("You have room for more calories. Consider adding a healthy snack.")
            }

            _suggestions.value = suggestions
        }
    }

    private fun getMealType(): String {
        val cal = Calendar.getInstance()
        return when (cal.get(Calendar.HOUR_OF_DAY)) {
            in 6..10 -> "Breakfast"
            in 12..15 -> "Lunch"
            in 18..21 -> "Dinner"
            else -> "Snack"
        }
    }

    private fun toFoodItemUi(foodItem: FoodItem): FoodItemUi {
        return FoodItemUi(
            id = 0, // Not available in FoodItem
            name = foodItem.name,
            calories = foodItem.calories,
            protein = foodItem.protein,
            carbs = foodItem.carbs,
            fat = foodItem.fats,
            servingQuantity = 100.0, // Default to 100g
            servingUnit = "g"
        )
    }
}
