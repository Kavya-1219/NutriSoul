package com.simats.nutrisoul.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodLogRepository @Inject constructor(private val dao: FoodLogDao) {

    fun observeLast7Days(email: String, startMillis: Long, endMillis: Long): Flow<List<FoodLogEntity>> {
        return dao.observeLogsBetween(email, startMillis, endMillis)
    }

    suspend fun addLog(log: FoodLogEntity) = dao.insert(log)
}
