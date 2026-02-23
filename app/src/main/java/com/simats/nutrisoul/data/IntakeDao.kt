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
            SUM(calories) as calories,
            SUM(protein) as protein,
            SUM(carbs) as carbs,
            SUM(fats) as fats
        FROM daily_intake
        WHERE date = :date
    """)
    fun observeTotalsForDate(date: LocalDate): Flow<DailyTotals>
}
