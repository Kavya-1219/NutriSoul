package com.simats.nutrisoul

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.FoodRepository
import com.simats.nutrisoul.data.models.DailyTotals
import com.simats.nutrisoul.ui.DailyTotalsUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val todayTotals: StateFlow<DailyTotalsUi> =
        repository.observeTodayTotals()
            .map<DailyTotals, DailyTotalsUi> { totals ->
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

    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    dailyTip = "Start your day with a glass of warm water to boost your metabolism.",
                    recentHistory = listOf("Chicken Salad", "Apple", "Protein Shake"),
                    aiTips = listOf(
                        "Your protein intake is a bit low. Consider adding some grilled chicken or lentils to your next meal.",
                        "You're doing great with your calorie goal! Keep it up."
                    )
                )
            }
        }
    }
}

data class HomeUiState(
    val dailyTip: String = "",
    val recentHistory: List<String> = emptyList(),
    val aiTips: List<String> = emptyList()
)
