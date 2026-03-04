package com.simats.nutrisoul.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val name: String,
    val caloriesPerUnit: Int,
    val proteinPerUnit: Int,
    val carbsPerUnit: Int,
    val fatsPerUnit: Int,
    val quantity: Float,
    val unit: String,
    val timestampMillis: Long
)
