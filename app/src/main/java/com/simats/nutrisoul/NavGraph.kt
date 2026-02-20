package com.simats.nutrisoul

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.simats.nutrisoul.data.UserRepository
import com.simats.nutrisoul.data.UserViewModel
import com.simats.nutrisoul.data.UserViewModelFactory

@Composable
fun NavGraph(navController: NavHostController, repository: UserRepository) {
    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(repository))

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(onTimeout = { navController.navigate(Screen.Onboarding1.route) })
        }
        composable(Screen.Onboarding1.route) {
            OnboardingScreen(
                onNextClicked = { navController.navigate(Screen.Onboarding2.route) },
                onSkipClicked = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.Onboarding2.route) {
            OnboardingScreen2(
                onNextClicked = { navController.navigate(Screen.Onboarding3.route) },
                onSkipClicked = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.Onboarding3.route) {
            OnboardingScreen3(
                onGetStartedClicked = { navController.navigate("register") },
                onSkipClicked = { navController.navigate(Screen.Login.route) },
                onLoginClicked = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController = navController)
        }
        composable(Screen.PersonalDetails.route) {
            PersonalDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onContinueClicked = { navController.navigate(Screen.BodyDetails.route) },
                userViewModel = userViewModel
            )
        }
        composable(Screen.BodyDetails.route) {
            BodyDetailsScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(Screen.FoodPreferences.route) {
            FoodPreferencesScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(Screen.LifestyleAndActivity.route) {
            LifestyleAndActivityScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(Screen.Goals.route) {
            GoalsScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(Screen.GoalWeight.route) {
            GoalWeightScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(Screen.HealthConditions.route) {
            HealthConditionsScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(Screen.HealthDetails.route) {
            HealthDetailsScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(Screen.MealsPerDay.route) {
            MealPerDayScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(Screen.WaterTracking.route) {
            WaterTrackingScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(Screen.StepsTracking.route) {
            StepsTrackingScreen(navController = navController)
        }
        composable(Screen.LogFood.route) {
            LogFoodScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(Screen.MealPlan.route) {
            MealPlanScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(Screen.AiTips.route) {
            AiTipsScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(Screen.StressAndSleep.route) {
            StressAndSleepScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(Screen.Recipes.route) {
            RecipesScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(Screen.Insights.route) {
            InsightsScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController, userViewModel = userViewModel)
        }
    }
}
