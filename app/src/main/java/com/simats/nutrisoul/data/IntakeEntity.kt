package com.simats.nutrisoul.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_intake")
data class IntakeEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,

    val quantity: Double,     // grams or servings

    val mealType: String,     // breakfast, lunch, dinner, snack

    val date: LocalDate       // today
)
