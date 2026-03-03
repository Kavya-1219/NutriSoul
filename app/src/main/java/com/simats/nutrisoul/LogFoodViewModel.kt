package com.simats.nutrisoul

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.FoodRepository
import com.simats.nutrisoul.data.models.DailyTotals
import com.simats.nutrisoul.data.models.FoodItem
import com.simats.nutrisoul.data.models.FoodLog
import com.simats.nutrisoul.ui.DailyTotalsUi
import com.simats.nutrisoul.ui.FoodItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject

data class LogFoodUiState(
    val isLoading: Boolean = false,
    val imageUri: Uri? = null,
    val extractedText: String = "",
    val detectedFoods: List<String> = emptyList(),
    val nutrition: List<FoodItemUi> = emptyList(),
    val error: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class LogFoodViewModel @Inject constructor(
    private val repository: FoodRepository,
    private val app: Application
) : AndroidViewModel(app) {

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
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DailyTotalsUi()
            )

    private val _uiState = MutableStateFlow(LogFoodUiState())
    val uiState: StateFlow<LogFoodUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _searchResults = MutableStateFlow<List<FoodItemUi>>(emptyList())
    val searchResults: StateFlow<List<FoodItemUi>> = _searchResults

    init {
        viewModelScope.launch {
            _query
                .debounce(300)
                .map { it.trim() }
                .distinctUntilChanged()
                .filter { it.length >= 2 }
                .flatMapLatest { q: String ->
                    repository.searchFoods(q, BuildConfig.NUTRITION_API_KEY)
                        .catch { e ->
                            Log.e("LogFood", "Search error", e)
                            emit(emptyList<FoodItem>())
                        }
                }
                .map { list: List<FoodItem> ->
                    list.map(::toFoodItemUi)
                }
                .collect { uiList: List<FoodItemUi> ->
                    _searchResults.value = uiList
                }
        }
    }

    fun onQueryChanged(query: String) {
        _query.value = query
    }

    fun clearScanState() {
        _uiState.value = LogFoodUiState()
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    imageUri = uri,
                    isLoading = true,
                    error = null,
                    extractedText = "",
                    detectedFoods = emptyList(),
                    nutrition = emptyList()
                )
            }

            try {
                val (text, foodsDetectedByOcr) = withContext(Dispatchers.Default) {
                    val t = OcrUtil.recognizeText(getApplication(), uri)
                    val f = FoodParser.extractFoods(t)
                    t to f
                }

                var finalFoods = foodsDetectedByOcr

                // If OCR found nothing, use ML Kit Image Labeling
                if (finalFoods.isEmpty()) {
                    val labels = LabelUtil.labelImage(getApplication(), uri)
                    // Map labels to our known food candidates or just use the top labels
                    finalFoods = labels.take(3) 
                    Log.d("LogFood", "OCR empty, ML labels: $finalFoods")
                }

                if (finalFoods.isEmpty()) {
                    _uiState.update {
                        it.copy(isLoading = false, extractedText = text, error = "No food items detected.")
                    }
                    return@launch
                }

                val apiKey = BuildConfig.NUTRITION_API_KEY

                val foundItems: List<FoodItem> = coroutineScope {
                    finalFoods.distinct().take(5).map { foodName ->
                        async(Dispatchers.IO) {
                            val list: List<FoodItem> = repository.searchFoods(foodName, apiKey).first()
                            list.firstOrNull()
                        }
                    }.awaitAll().filterNotNull()
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        extractedText = text,
                        detectedFoods = finalFoods,
                        nutrition = foundItems.map(::toFoodItemUi),
                        error = if (foundItems.isEmpty()) "Found $finalFoods but couldn't fetch nutrition details." else null
                    )
                }
            } catch (e: Exception) {
                Log.e("LogFood", "Scan failed", e)
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Scan failed") }
            }
        }
    }

    fun addFood(foodItem: FoodItemUi, quantity: Double) {
        viewModelScope.launch {
            val scale = if (foodItem.servingQuantity != 0.0) quantity / foodItem.servingQuantity else 0.0
            val foodLog = FoodLog(
                name = foodItem.name,
                calories = foodItem.calories * scale,
                protein = foodItem.protein * scale,
                carbs = foodItem.carbs * scale,
                fats = foodItem.fat * scale,
                quantity = quantity,
                mealType = getMealType(),
                date = LocalDate.now()
            )
            repository.addFoodToDailyIntake(foodLog)
        }
    }

    fun addManualFood(
        name: String,
        quantity: Double,
        unit: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fats: Double
    ) {
        viewModelScope.launch {
            val foodLog = FoodLog(
                name = name,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fats = fats,
                quantity = quantity,
                mealType = "Manual Entry",
                date = LocalDate.now()
            )
            repository.addFoodToDailyIntake(foodLog)
        }
    }

    fun getTargetCaloriesOrDefault(default: Double): Double {
        return default
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
            id = 0,
            name = foodItem.name,
            calories = foodItem.calories,
            protein = foodItem.protein,
            carbs = foodItem.carbs,
            fat = foodItem.fats,
            servingQuantity = 100.0,
            servingUnit = "g"
        )
    }
}
