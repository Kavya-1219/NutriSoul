package com.simats.nutrisoul.data

import com.simats.nutrisoul.data.models.DailyTotals
import com.simats.nutrisoul.data.models.FoodItem
import com.simats.nutrisoul.data.models.FoodLog
import com.simats.nutrisoul.data.models.toCustomFoodEntity
import com.simats.nutrisoul.data.models.toFoodItem
import com.simats.nutrisoul.data.models.toIntakeEntity
import com.simats.nutrisoul.data.models.toFoodLog
import com.simats.nutrisoul.data.network.NutritionApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FoodRepository(
    private val customFoodDao: CustomFoodDao,
    private val nutritionApiService: NutritionApiService,
    private val intakeDao: IntakeDao
) {

    fun getTodaysLoggedFoods(): Flow<List<FoodLog>> {
        return intakeDao.getForDate(LocalDate.now()).map { list -> list.map { it.toFoodLog() } }
    }

    fun searchFoods(query: String, apiKey: String): Flow<List<FoodItem>> {
        val localResults = customFoodDao.search(query).map { list -> list.map { it.toFoodItem() } }
        val remoteResults = flow {
            try {
                val results = nutritionApiService.searchFoods(query, apiKey).body()?.foods?.map { it.toFoodItem() } ?: emptyList()
                emit(results)
            } catch (e: Exception) {
                // Handle API error
                emit(emptyList())
            }
        }
        return combine(localResults, remoteResults) { local, remote -> local + remote }
    }

    suspend fun addFoodToDailyIntake(foodLog: FoodLog) {
        intakeDao.insert(foodLog.toIntakeEntity())
    }

    suspend fun saveCustomFood(foodItem: FoodItem) {
        customFoodDao.insert(foodItem.toCustomFoodEntity())
    }

    fun observeTodayTotals(): Flow<DailyTotals> {
        return intakeDao.observeTotalsForDate(LocalDate.now())
    }
}
