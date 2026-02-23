package com.simats.nutrisoul

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Welcome : Screen("welcome", "Welcome", Icons.Default.Home) 
    object SignUp : Screen("signup", "Sign Up", Icons.Default.Home) 
    object Login : Screen("login", "Login", Icons.Default.Home) 
    object AgeGender : Screen("age_gender", "Age & Gender", Icons.Default.Home) 
    object HeightWeight : Screen("height_weight", "Height & Weight", Icons.Default.Home) 
    object Goal : Screen("goal", "Goal", Icons.Default.Home) 
    object HealthConditions : Screen("health_conditions", "Health Conditions", Icons.Default.Home) 
    object HealthDetails : Screen("health_details", "Health Details", Icons.Default.Home) 
    object MealsPerDay : Screen("meals_per_day", "Meals Per Day", Icons.Default.Home) 
    object DietaryPreferences : Screen("dietary_preferences", "Dietary Preferences", Icons.Default.Home) 
    object CuisinePreferences : Screen("cuisine_preferences", "Cuisine Preferences", Icons.Default.Home) 
    object Home : Screen("home", "Home", Icons.Default.Home)
    object FoodPreferences : Screen("food_preferences", "Food Preferences", Icons.Default.Home) 
    object LifestyleAndActivity : Screen("lifestyle_and_activity", "Lifestyle & Activity", Icons.Default.Home) 
    object GoalWeight : Screen("goal_weight", "Goal Weight", Icons.Default.Home) 
    object WaterTracking : Screen("water_tracking", "Water Tracking", Icons.Default.Home) 
    object StepsTracking : Screen("steps_tracking", "Steps Tracking", Icons.Default.Home) 
    object LogFood : Screen("log_food", "Log Food", Icons.Default.Home) 
    object AiTips : Screen("ai_tips", "AI Tips", Icons.Default.Home) 
    object ManualFoodEntry : Screen("manual_food_entry", "Manual Food Entry", Icons.Default.Home) 
    object ScanFood : Screen("scan_food", "Scan Food", Icons.Default.Home) 
    object Register : Screen("register", "Register", Icons.Default.Home) 
    object Splash : Screen("splash", "Splash", Icons.Default.Home) 
    object Onboarding1 : Screen("onboarding1", "Onboarding", Icons.Default.Home) 
    object Onboarding2 : Screen("onboarding2", "Onboarding", Icons.Default.Home) 
    object Onboarding3 : Screen("onboarding3", "Onboarding", Icons.Default.Home) 
    object ForgotPassword : Screen("forgot_password", "Forgot Password", Icons.Default.Home) 
    object PersonalDetails : Screen("personal_details", "Personal Details", Icons.Default.Home) 
    object BodyDetails : Screen("body_details", "Body Details", Icons.Default.Home) 
    object Goals : Screen("goals", "Goals", Icons.Default.Home) 
    object MealPlan : Screen("meal_plan", "Meal Plan", Icons.Default.Home) 
    object History : Screen("history", "History", Icons.Default.Home) 
    object StressAndSleep : Screen("stress_and_sleep", "Stress & Sleep", Icons.Default.Bedtime)
    object Recipes : Screen("recipes", "Recipes", Icons.Default.MenuBook)
    object Insights : Screen("insights", "Insights", Icons.Default.Insights)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object TodaysMealPlan : Screen("todays_meal_plan", "Today's Meal Plan", Icons.Default.Home)
}
