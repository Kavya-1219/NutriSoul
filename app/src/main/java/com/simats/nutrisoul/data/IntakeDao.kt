package com.simats.nutrisoul.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simats.nutrisoul.data.models.DailyTotals
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface IntakeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: IntakeEntity)

    @Query("""
        SELECT * FROM daily_intake
        WHERE date = :date
        ORDER BY id DESC
    """)
    fun getForDate(date: LocalDate): Flow<List<IntakeEntity>>

    @Query("""
        SELECT 
            IFNULL(SUM(calories), 0) as calories,
            IFNULL(SUM(protein), 0) as protein,
            IFNULL(SUM(carbs), 0) as carbs,
            IFNULL(SUM(fats), 0) as fats
        FROM daily_intake
        WHERE date = :date
    """)
    fun observeTotalsForDate(date: LocalDate): Flow<DailyTotals>
}
