package com.simats.nutrisoul.data

import com.simats.nutrisoul.data.models.DailyTotals
import com.simats.nutrisoul.data.models.FoodItem
import com.simats.nutrisoul.data.models.FoodLog
import com.simats.nutrisoul.data.network.NutritionApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate
import javax.inject.Inject

class FoodRepository @Inject constructor(
    private val foodDao: FoodDao,
    private val intakeDao: IntakeDao,
    private val customFoodDao: CustomFoodDao,
    private val nutritionApiService: NutritionApiService
) {

    fun observeTodayTotals(): Flow<DailyTotals> {
        return intakeDao.observeTotalsForDate(LocalDate.now())
    }

    fun searchFoods(query: String, apiKey: String): Flow<List<FoodItem>> {
        return flow {
            try {
                val response = nutritionApiService.searchFoods(query, apiKey)
                if (response.isSuccessful) {
                    emit(response.body()?.toFoodItems() ?: emptyList())
                } else {
                    emit(emptyList<FoodItem>())
                }
            } catch (e: Exception) {
                emit(emptyList<FoodItem>())
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun addFoodToDailyIntake(foodLog: FoodLog) {
        intakeDao.insert(foodLog.toEntity())
    }

    suspend fun saveCustomFood(foodItem: FoodItem) {
        customFoodDao.insert(foodItem.toEntity())
    }
}
