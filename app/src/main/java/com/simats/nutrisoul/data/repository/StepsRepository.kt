package com.simats.nutrisoul.data.repository

import com.simats.nutrisoul.data.datastore.UserPreferencesRepository
import com.simats.nutrisoul.data.health.HealthConnectManager
import com.simats.nutrisoul.data.StepsDao
import com.simats.nutrisoul.data.StepsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class StepsRepository @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val stepsDao: StepsDao,
    private val userPreferences: UserPreferencesRepository,
    // private val apiService: StepsApiService // Inject when API is ready
) {
    val todaySteps: Flow<StepsEntity?> = stepsDao.getStepsForDate(LocalDate.now())

    fun getWeeklySteps(): Flow<List<StepsEntity>> {
        val sevenDaysAgo = LocalDate.now().minus(6, ChronoUnit.DAYS)
        return stepsDao.getStepsFrom(sevenDaysAgo)
    }

    val stepsGoal: Flow<Int> = userPreferences.stepsGoal

    suspend fun setStepsGoal(goal: Int) {
        userPreferences.setStepsGoal(goal)
    }

    /**
     * Reads steps from Health Connect, saves to Room, and syncs to the backend.
     */
    suspend fun syncSteps() {
        val stepsCount = healthConnectManager.readTodaySteps()
        val goal = stepsGoal.first()

        val entity = StepsEntity(
            date = LocalDate.now(),
            steps = stepsCount,
            goal = goal
        )
        
        stepsDao.upsert(entity)
        
        // TODO: Sync with backend
        // try {
        //     apiService.postSteps(date = entity.date, steps = entity.steps)
        // } catch (e: Exception) {
        //     // Handle API errors
        // }
    }
}
