package com.simats.nutrisoul.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

@Entity(tableName = "user_profile")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val height: Float = 0f,
    val weight: Float = 0f,
    val activityLevel: String = "",
    val goal: String = "",
    val targetWeight: Float = 0f,
    val currentWeight: Float = 0f,
    val mealsPerDay: Int = 0,
    val healthConditions: List<String> = emptyList(),
    val todaysCalories: Int = 0,
    val todaysWaterIntake: Int = 0,
    val todaysSteps: Int = 0,
    val bmr: Int = 0,
    val lastLogin: Timestamp = Timestamp.now(),
    val allergies: List<String> = emptyList(),
    val foodAllergies: List<String> = emptyList(),
    val cholesterolLevel: String = "",
    val password: String = "",
    val dietaryRestrictions: List<String> = emptyList(),
    val otherAllergies: String = "",
    val email: String = "",
    val dislikes: List<String> = emptyList(),
    val diabetesType: String = "",
    val diastolic: Int = 0,
    val systolic: Int = 0,
    val thyroidCondition: String = "",
    val targetCalories: Int = 0
)
