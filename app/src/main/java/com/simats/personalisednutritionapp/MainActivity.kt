package com.simats.personalisednutritionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simats.personalisednutritionapp.ui.theme.PersonalisedNutritionAppTheme
import com.simats.personalisednutritionapp.BodyDetailsScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonalisedNutritionAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        // Splash Screen
        composable("splash") {
            SplashScreen(onTimeout = {
                navController.navigate("onboarding1") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }


        // Onboarding Screen 1
        composable("onboarding1") {
            OnboardingScreen(
                onNextClicked = {
                    navController.navigate("onboarding2")
                },
                onSkipClicked = {
                    navController.navigate("login") {
                        popUpTo("onboarding1") { inclusive = true }
                    }
                }
            )
        }

        // Onboarding Screen 2
        composable("onboarding2") {
            OnboardingScreen2(
                onNextClicked = {
                    navController.navigate("onboarding3")
                },
                onSkipClicked = {
                    navController.navigate("login") {
                        popUpTo("onboarding1") { inclusive = true }
                    }
                }
            )
        }

        // Onboarding Screen 3
        composable("onboarding3") {
            OnboardingScreen3(
                onSkipClicked = {
                    navController.navigate("login") {
                        popUpTo("onboarding1") { inclusive = true }
                    }
                },
                onLoginClicked = {
                    navController.navigate("login")
                },
                onGetStartedClicked = {
                    navController.navigate("register")
                }
            )
        }

        // Login Screen
        composable("login") {
            LoginScreen(
                onLoginClicked = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onCreateNewAccountClicked = {
                    navController.navigate("register")
                }
            )
        }

        // Home Screen
        composable("home") {
            HomeScreen()
        }

        // Register Screen
        composable("register") {
            RegisterScreen(
                onAccountCreated = {
                    navController.navigate("personalDetails")
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLoginClicked = {
                    navController.navigate("login")
                }
            )
        }

        // Personal Details
        composable("personalDetails") {
            PersonalDetailsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onContinueClicked = {
                    navController.navigate("bodyDetails")
                }
            )
        }

        // Body Details
        composable("bodyDetails") {
            BodyDetailsScreen(navController = navController)
    }

        // Food Preference Screen
        composable("foodPreferences") {
            FoodPreferencesScreen(navController = navController)
        }

        // Lifestyle & Activity
        composable("lifestyle") {
            LifestyleAndActivityScreen(navController = navController)
        }

        // Goals Screen
        composable("goals") {
            GoalsScreen(navController = navController)
        }

        // Goal Weight Screen
        composable("goalWeight") {
            GoalWeightScreen(navController = navController)
        }

        // Health Conditions Screen
        composable("healthConditions") {
            HealthConditionsScreen(navController = navController)
        }

        // Health Details Screen
        composable(
            "healthDetails/{conditionNames}",
            arguments = listOf(navArgument("conditionNames") { type = NavType.StringType })
        ) {
            HealthDetailsScreen(
                navController = navController,
                conditionNames = it.arguments?.getString("conditionNames")
            )
        }

        // Meal Per Day Screen
        composable("mealPerDay") {
            MealPerDayScreen(navController = navController)
        }
    }
}
