package com.simats.nutrisoul

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.FoodLogEntity
import com.simats.nutrisoul.data.FoodLogRepository
import com.simats.nutrisoul.data.FoodRepository
import com.simats.nutrisoul.data.SessionManager
import com.simats.nutrisoul.data.UserRepository
import com.simats.nutrisoul.data.models.DailyTotals
import com.simats.nutrisoul.data.models.FoodItem
import com.simats.nutrisoul.data.models.FoodLog
import com.simats.nutrisoul.ui.DailyTotalsUi
import com.simats.nutrisoul.ui.FoodItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
import kotlinx.coroutines.flow.flowOf
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

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class LogFoodViewModel @Inject constructor(
    private val repository: FoodRepository,
    private val foodLogRepository: FoodLogRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val app: Application
) : AndroidViewModel(app) {

    val todayTotals: StateFlow<DailyTotalsUi> =
        sessionManager.currentUserEmailFlow()
            .flatMapLatest { email ->
                if (email == null) flowOf(DailyTotals())
                else repository.observeTodayTotals(email)
            }
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

    suspend fun getTargetCaloriesOrDefault(default: Double): Double {
        return userRepository.getLatestUser().first()?.targetCalories?.toDouble() ?: default
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

                if (finalFoods.isEmpty()) {
                    val labels = LabelUtil.labelImage(getApplication(), uri)
                    finalFoods = labels.take(3) 
                }

                if (finalFoods.isEmpty()) {
                    _uiState.update {
                        it.copy(isLoading = false, extractedText = text, error = "No food items detected.")
                    }
                    return@launch
                }

                val apiKey = BuildConfig.NUTRITION_API_KEY

                val foundItems: List<FoodItemUi> = coroutineScope {
                    finalFoods.distinct().take(3).flatMap { foodName ->
                        val results = repository.searchFoods(foodName, apiKey).first()
                        results.take(3).map(::toFoodItemUi)
                    }
                }

                val localFallback = finalFoods
                    .flatMap { key ->
                        suggestedFoods.filter { it.name.contains(key, ignoreCase = true) }
                    }
                    .distinctBy { it.name }
                    .take(5)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        extractedText = text,
                        detectedFoods = finalFoods,
                        nutrition = if (foundItems.isNotEmpty()) foundItems else localFallback,
                        error = if (foundItems.isEmpty() && localFallback.isEmpty())
                            "Found $finalFoods but couldn't fetch nutrition details."
                        else null
                    )
                }
            } catch (e: Exception) {
                Log.e("LogFood", "Scan failed", e)
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Scan failed") }
            }
        }
    }

    fun addFood(foodItem: FoodItemUi, grams: Double) {
        viewModelScope.launch {
            val email = sessionManager.currentUserEmailFlow().first() ?: return@launch

            // FoodItemUi values are per 100g
            val gramsSafe = grams.coerceAtLeast(1.0)
            val qty100 = gramsSafe / 100.0

            // 1) Save to food_logs (now storing Float to preserve precision)
            foodLogRepository.addLog(
                FoodLogEntity(
                    userEmail = email,
                    name = foodItem.name,
                    caloriesPerUnit = foodItem.calories.toFloat(),
                    proteinPerUnit = foodItem.protein.toFloat(),
                    carbsPerUnit = foodItem.carbs.toFloat(),
                    fatsPerUnit = foodItem.fats.toFloat(),
                    quantity = qty100.toFloat(),
                    unit = "100g",
                    timestampMillis = System.currentTimeMillis()
                )
            )

            // 2) Save to daily_intake totals (already scaled)
            val foodLog = FoodLog(
                name = foodItem.name,
                calories = foodItem.calories * qty100,
                protein = foodItem.protein * qty100,
                carbs = foodItem.carbs * qty100,
                fats = foodItem.fats * qty100,
                quantity = gramsSafe,
                mealType = getMealType(),
                date = LocalDate.now()
            )
            repository.addFoodToDailyIntake(email, foodLog)
        }
    }

    fun addManualFood(
        name: String,
        quantity: Double,
        calories: Double,
        protein: Double,
        carbs: Double,
        fats: Double
    ) {
        viewModelScope.launch {
            val email = sessionManager.currentUserEmailFlow().first() ?: return@launch

            val gramsSafe = quantity.coerceAtLeast(1.0)
            val qty100 = gramsSafe / 100.0
            val per100Factor = 100.0 / gramsSafe

            // Convert totals into per-100g storage as Float
            val caloriesPer100 = (calories * per100Factor).toFloat()
            val proteinPer100 = (protein * per100Factor).toFloat()
            val carbsPer100 = (carbs * per100Factor).toFloat()
            val fatsPer100 = (fats * per100Factor).toFloat()

            // 1) Save to food_logs
            foodLogRepository.addLog(
                FoodLogEntity(
                    userEmail = email,
                    name = name,
                    caloriesPerUnit = caloriesPer100,
                    proteinPerUnit = proteinPer100,
                    carbsPerUnit = carbsPer100,
                    fatsPerUnit = fatsPer100,
                    quantity = qty100.toFloat(),
                    unit = "100g",
                    timestampMillis = System.currentTimeMillis()
                )
            )

            // 2) Save to daily_intake (totals already correct)
            val foodLog = FoodLog(
                name = name,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fats = fats,
                quantity = gramsSafe,
                mealType = "Manual Entry",
                date = LocalDate.now()
            )
            repository.addFoodToDailyIntake(email, foodLog)
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
            id = foodItem.id,
            name = foodItem.name,
            calories = foodItem.caloriesPer100g,
            protein = foodItem.proteinPer100g,
            carbs = foodItem.carbsPer100g,
            fats = foodItem.fatsPer100g,
            servingQuantity = foodItem.servingQuantity,
            servingUnit = foodItem.servingUnit
        )
    }
}
