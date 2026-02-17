package com.simats.personalisednutritionapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "user_profile")
@TypeConverters(StringListConverter::class, DateConverter::class)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val age: Int = 0,
    val gender: String = "",
    val height: Double = 0.0,
    val currentWeight: Double = 0.0,
    val targetWeight: Double = 0.0,
    val goal: String = "",
    val targetCalories: Double = 0.0,
    val todaysCalories: Double = 0.0,
    val todaysWaterIntake: Int = 0,
    val dietaryRestrictions: String = "",
    val allergies: List<String> = emptyList(),
    val dislikes: List<String> = emptyList(),
    val activityLevel: String = "",
    val lastLogin: Date = Date(),
    val healthConditions: List<String> = emptyList(),
    val mealsPerDay: Int = 3,
    // New Health Details
    val systolic: Int? = null,
    val diastolic: Int? = null,
    val thyroidCondition: String? = null,
    val diabetesType: String? = null,
    val cholesterolLevel: Int? = null,
    val foodAllergies: List<String> = emptyList(),
    val otherAllergies: String? = null
)
