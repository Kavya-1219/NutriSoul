package com.simats.nutrisoul

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.nutrisoul.data.FoodLogEntity
import com.simats.nutrisoul.data.FoodLogRepository
import com.simats.nutrisoul.data.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

@HiltViewModel
class NutritionInsightsViewModel @Inject constructor(
    private val repo: FoodLogRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val zoneId = ZoneId.systemDefault()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<NutritionInsightsUiState> =
        sessionManager.currentUserEmailFlow()
            .flatMapLatest { email ->
                if (email.isNullOrBlank()) {
                    flowOf(NutritionInsightsUiState.Empty)
                } else {
                    val (startMillis, endMillis) = last7DaysRangeMillis()
                    repo.observeLast7Days(email, startMillis, endMillis)
                        .map { logs ->
                            if (logs.isEmpty()) return@map NutritionInsightsUiState.Empty

                            // Debug log to verify data in Room
                            logs.take(5).forEach {
                                Log.d(
                                    "INSIGHTS_DEBUG",
                                    "Food=${it.name}, cal=${it.caloriesPerUnit}, p=${it.proteinPerUnit}, c=${it.carbsPerUnit}, f=${it.fatsPerUnit}, q=${it.quantity}"
                                )
                            }

                            // Group logs by day
                            val grouped = logs.groupBy { log ->
                                Instant.ofEpochMilli(log.timestampMillis)
                                    .atZone(zoneId)
                                    .toLocalDate()
                            }

                            val daysLogged = grouped.keys.size.coerceIn(0, 7)

                            // Daily totals per day
                            val dailyTotals: List<DayTotals> = grouped.values.map { dayLogs ->
                                dayLogs.fold(DayTotals()) { acc, item ->
                                    val macros = mapToMacros(item)
                                    acc.copy(
                                        calories = acc.calories + macros.calories,
                                        protein = acc.protein + macros.protein,
                                        carbs = acc.carbs + macros.carbs,
                                        fats = acc.fats + macros.fats
                                    )
                                }
                            }

                            val avgCalories =
                                (dailyTotals.sumOf { it.calories.toDouble() } / dailyTotals.size).roundToInt()
                            val avgProtein =
                                (dailyTotals.sumOf { it.protein.toDouble() } / dailyTotals.size).toFloat()
                            val avgCarbs =
                                (dailyTotals.sumOf { it.carbs.toDouble() } / dailyTotals.size).toFloat()
                            val avgFats =
                                (dailyTotals.sumOf { it.fats.toDouble() } / dailyTotals.size).toFloat()

                            // Frontend-only target (replace later when profile/backend is ready)
                            val targetCalories = 2000

                            val consistencyPercent = ((daysLogged / 7f) * 100).roundToInt()
                            val weeklyConsistency = (daysLogged / 7f).coerceIn(0f, 1f)

                            val status = calorieStatus(avgCalories, targetCalories)
                            val macroPct = macroPercentFromCalories(
                                proteinG = avgProtein,
                                carbsG = avgCarbs,
                                fatsG = avgFats
                            )

                            NutritionInsightsUiState.Success(
                                NutritionInsightsData(
                                    weeklyConsistency = weeklyConsistency,
                                    consistencyPercent = consistencyPercent,
                                    daysLogged = daysLogged,
                                    totalDays = 7,
                                    averageCalories = avgCalories,
                                    targetCalories = targetCalories,
                                    averageProtein = avgProtein,
                                    averageCarbs = avgCarbs,
                                    averageFats = avgFats,
                                    proteinPercentage = macroPct.protein,
                                    carbsPercentage = macroPct.carbs,
                                    fatsPercentage = macroPct.fats,
                                    calorieStatus = status
                                )
                            )
                        }
                        .onStart { emit(NutritionInsightsUiState.Loading) }
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                NutritionInsightsUiState.Loading
            )

    // ---------------- Calculations ----------------

    private fun last7DaysRangeMillis(): Pair<Long, Long> {
        val today = LocalDate.now(zoneId)
        val start = today.minusDays(6) // inclusive: today + last 6 days = 7 days
        val startMillis = start.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMillis = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return startMillis to endMillis
    }

    private data class DayTotals(
        val calories: Float = 0f,
        val protein: Float = 0f,
        val carbs: Float = 0f,
        val fats: Float = 0f
    )

    // ✅ matches your FoodLogEntity
    private fun mapToMacros(log: FoodLogEntity): DayTotals {
        val q = log.quantity.coerceAtLeast(0f)
        return DayTotals(
            calories = log.caloriesPerUnit * q,
            protein = log.proteinPerUnit * q,
            carbs = log.carbsPerUnit * q,
            fats = log.fatsPerUnit * q
        )
    }

    private fun calorieStatus(avg: Int, target: Int): CalorieStatus {
        if (target <= 0) return CalorieStatus("Unknown", StatusTone.NEUTRAL, "ℹ️")
        val diff = avg - target
        val percentDiff = abs(diff / target.toFloat()) * 100f
        return when {
            percentDiff < 5f -> CalorieStatus("Excellent", StatusTone.GOOD, "🎯")
            percentDiff < 10f -> CalorieStatus("Good", StatusTone.OK, "👍")
            diff > 0 -> CalorieStatus("Over Target", StatusTone.WARN, "⚠️")
            else -> CalorieStatus("Under Target", StatusTone.INFO, "📉")
        }
    }

    private data class MacroPercent(val protein: Int, val carbs: Int, val fats: Int)

    private fun macroPercentFromCalories(
        proteinG: Float,
        carbsG: Float,
        fatsG: Float
    ): MacroPercent {
        val macroCals = (proteinG * 4f) + (carbsG * 4f) + (fatsG * 9f)
        val denom = macroCals.coerceAtLeast(1f)
        val proteinPct = ((proteinG * 4f) / denom * 100f).roundToInt().coerceIn(0, 100)
        val carbsPct = ((carbsG * 4f) / denom * 100f).roundToInt().coerceIn(0, 100)
        val fatsPct = ((fatsG * 9f) / denom * 100f).roundToInt().coerceIn(0, 100)
        return MacroPercent(proteinPct, carbsPct, fatsPct)
    }
}

// ---------------- UI models ----------------

sealed class NutritionInsightsUiState {
    data object Loading : NutritionInsightsUiState()
    data object Empty : NutritionInsightsUiState()
    data class Success(val data: NutritionInsightsData) : NutritionInsightsUiState()
}

data class NutritionInsightsData(
    val weeklyConsistency: Float,
    val consistencyPercent: Int,
    val daysLogged: Int,
    val totalDays: Int,
    val averageCalories: Int,
    val targetCalories: Int,
    val averageProtein: Float,
    val averageCarbs: Float,
    val averageFats: Float,
    val proteinPercentage: Int,
    val carbsPercentage: Int,
    val fatsPercentage: Int,
    val calorieStatus: CalorieStatus
)

data class CalorieStatus(
    val label: String,
    val tone: StatusTone,
    val emoji: String
)

enum class StatusTone { GOOD, OK, WARN, INFO, NEUTRAL }
