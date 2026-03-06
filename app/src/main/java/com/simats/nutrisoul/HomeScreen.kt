package com.simats.nutrisoul

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.simats.nutrisoul.data.User
import com.simats.nutrisoul.data.UserViewModel
import com.simats.nutrisoul.steps.StepTrackingService
import com.simats.nutrisoul.ui.DailyTotalsUi
import com.simats.nutrisoul.ui.steps.StepsViewModel
import com.simats.nutrisoul.ui.theme.LocalDarkTheme
import com.simats.nutrisoul.ui.theme.PrimaryGreen
import kotlin.math.abs
import java.util.Calendar

@Composable
fun HomeScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    homeViewModel: HomeViewModel = hiltViewModel(),
    stepsViewModel: StepsViewModel = hiltViewModel()
) {
    val user by userViewModel.user.collectAsStateWithLifecycle()
    val isLoading by userViewModel.isLoading.collectAsStateWithLifecycle()

    val totals by homeViewModel.todayTotals.collectAsStateWithLifecycle()
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    val stepsToday by stepsViewModel.todaySteps.collectAsStateWithLifecycle()
    val autoEnabled by stepsViewModel.autoEnabled.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(autoEnabled) {
        val intent = Intent(context, StepTrackingService::class.java)
        if (autoEnabled) context.startForegroundService(intent) else context.stopService(intent)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator(color = PrimaryGreen)
                user == null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error loading user data.", color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { userViewModel.retryLoadUserData() }) { Text("Retry") }
                    }
                }
                else -> {
                    HomeDashboardContent(
                        navController = navController,
                        user = user!!,
                        totals = totals,
                        ui = homeUiState,
                        userViewModel = userViewModel,
                        todaysSteps = stepsToday.toInt()
                    )
                }
            }
        }
    }
}

private fun dailyCalorieGoal(user: User): Int {
    val bmr = user.bmr.takeIf { it > 0 } ?: 1500

    val activityFactor = when (user.activityLevel?.lowercase()) {
        "sedentary" -> 1.2
        "light" -> 1.375
        "moderate" -> 1.55
        "active" -> 1.725
        else -> 1.375
    }

    val tdee = (bmr * activityFactor).toInt()

    val goalLower = user.goal.trim().lowercase()

    val safeAdjustment = if (goalLower.contains("lose") || goalLower.contains("gain")) {
        val weightDiff = user.targetWeight - user.currentWeight
        val totalCaloriesToShift = weightDiff * 7700
        val days = (user.targetWeeks.coerceAtLeast(1) * 7)
        (totalCaloriesToShift / days).toInt().coerceIn(-1000, 1000)
    } else 0
    
    val goal = tdee + safeAdjustment
    
    return goal.coerceAtLeast(1200)
}

private fun caloriesBurnedFromSteps(steps: Int, weightKg: Float): Int {
    val factor = 0.04 * (weightKg / 70f).coerceIn(0.6f, 1.6f)
    return (steps * factor).toInt().coerceAtLeast(0)
}

@Composable
private fun HomeDashboardContent(
    navController: NavController,
    user: User,
    totals: DailyTotalsUi,
    ui: HomeUiState,
    userViewModel: UserViewModel,
    todaysSteps: Int
) {
    val goal = dailyCalorieGoal(user).coerceIn(1200, 4500)

    val consumedCals = (totals.calories ?: 0.0).toInt().coerceAtLeast(0)
    val burnedCals = caloriesBurnedFromSteps(todaysSteps, user.currentWeight)
    val remaining = goal - consumedCals

    val profilePictureUri by userViewModel.profilePictureUri.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        DashboardHeader(
            name = user.name,
            profilePictureUri = profilePictureUri
        )

        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .offset(y = (-60).dp)
                .zIndex(2f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            DailyCaloriesCardFigma(
                consumed = consumedCals,
                target = goal,
                remaining = remaining,
                burned = burnedCals
            )
        }

        Spacer(modifier = Modifier.height((-40).dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (hasWeightGoal(user.goal, user.targetWeight)) {
                WeightProgressCardFigma(
                    currentWeight = user.currentWeight,
                    targetWeight = user.targetWeight,
                    goal = user.goal
                )
            }

            TodayActivitySection(
                navController = navController,
                waterMl = user.todaysWaterIntake.toInt(),
                steps = todaysSteps
            )

            QuickActionsCard(navController)

            DailyTipCardFigma(
                tip = ui.dailyTip.ifBlank {
                    if (consumedCals < goal / 2) "You're doing great! Make sure to eat enough to meet your daily goals."
                    else if (consumedCals > (goal * 1.2).toInt()) "You're over your calorie target. Try lighter options for your next meal."
                    else "You're on track! Keep up the good work with balanced nutrition."
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DashboardHeader(
    name: String,
    profilePictureUri: Uri?
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    val headerBrush = Brush.horizontalGradient(
        listOf(
            Color(0xFF22C55E),
            Color(0xFF16A34A)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
            .background(headerBrush)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    greeting,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 14.sp
                )
                Text(
                    name.ifBlank { "User" },
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                if (profilePictureUri != null) {
                    AsyncImage(
                        model = profilePictureUri,
                        contentDescription = "Profile picture",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("👤", fontSize = 22.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun DailyCaloriesCardFigma(
    consumed: Int,
    target: Int,
    remaining: Int,
    burned: Int
) {
    val safeTarget = target.coerceAtLeast(1)
    val progress = (consumed.toFloat() / safeTarget.toFloat()).coerceIn(0f, 1f)

    Column(modifier = Modifier.padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Daily Calorie Goal",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Outlined.LocalFireDepartment,
                contentDescription = null,
                tint = Color(0xFFF97316)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                consumed.toString(),
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "/ $target",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            val barBrush = if (consumed > target) {
                Brush.horizontalGradient(listOf(Color(0xFFF97316), Color(0xFFEF4444)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFF4ADE80), Color(0xFF16A34A)))
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(999.dp))
                    .background(barBrush)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (remaining >= 0) "Remaining: $remaining kcal" else "Over by: ${abs(remaining)} kcal",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "Burned: $burned kcal",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF16A34A)
            )
        }
    }
}

private fun hasWeightGoal(goal: String, targetWeight: Float): Boolean {
    val isLose = goal.equals("Weight Loss", ignoreCase = true) || goal.equals("Lose Weight", ignoreCase = true)
    val isGain = goal.equals("Gain Muscle", ignoreCase = true) || goal.equals("Gain Weight", ignoreCase = true)
    return (isLose || isGain) && targetWeight > 0f
}

@Composable
private fun WeightProgressCardFigma(
    currentWeight: Float,
    targetWeight: Float,
    goal: String
) {
    val isLose = goal.equals("Weight Loss", ignoreCase = true) || goal.equals("Lose Weight", ignoreCase = true)
    val icon = if (isLose) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp
    val isDark = LocalDarkTheme.current

    val cardBrush = if (isDark) {
        Brush.horizontalGradient(listOf(Color(0xFF1E3A8A).copy(alpha=0.3f), Color(0xFF312E81).copy(alpha=0.3f)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFEFF6FF), Color(0xFFE0E7FF)))
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(cardBrush)
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Weight Progress",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(icon, contentDescription = null, tint = if(isDark) Color(0xFF60A5FA) else Color(0xFF2563EB))
                }

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WeightMiniBox(label = "Current", value = "${currentWeight} kg", valueColor = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    WeightMiniBox(label = "Target", value = "${targetWeight} kg", valueColor = if(isDark) Color(0xFF60A5FA) else Color(0xFF2563EB), modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(10.dp))

                val diff = abs(currentWeight - targetWeight)
                Text(
                    text = "${"%.1f".format(diff)} kg to go",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeightMiniBox(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
private fun TodayActivitySection(
    navController: NavController,
    waterMl: Int,
    steps: Int
) {
    val isDark = LocalDarkTheme.current
    Column {
        Text(
            "Today's Activity",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            ActivityWidget(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.WaterDrop,
                iconTint = Color(0xFF3B82F6),
                titleValue = waterMl.toString(),
                subtitle = "ml water",
                background = if (isDark) {
                    Brush.linearGradient(listOf(Color(0xFF1E3A8A).copy(alpha=0.3f), Color(0xFF1E40AF).copy(alpha=0.3f)))
                } else {
                    Brush.linearGradient(listOf(Color(0xFFE0F2FE), Color(0xFFDBEAFE)))
                },
                onClick = { navController.navigate(Screen.WaterTracking.route) }
            )

            ActivityWidget(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                iconTint = Color(0xFF10B981),
                titleValue = steps.toString(),
                subtitle = "steps today",
                background = if (isDark) {
                    Brush.linearGradient(listOf(Color(0xFF064E3B).copy(alpha=0.3f), Color(0xFF065F46).copy(alpha=0.3f)))
                } else {
                    Brush.linearGradient(listOf(Color(0xFFECFDF5), Color(0xFFDCFCE7)))
                },
                onClick = { navController.navigate(Screen.StepsTracking.route) }
            )
        }
    }
}

@Composable
private fun ActivityWidget(
    modifier: Modifier,
    icon: ImageVector,
    iconTint: Color,
    titleValue: String,
    subtitle: String,
    background: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(16.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    titleValue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuickActionsCard(navController: NavController) {
    Column {
        Text(
            "Quick Actions",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                QuickActionItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Add,
                    label = "Log Food",
                    description = "Track your meals",
                    onClick = { navController.navigate(Screen.LogFood.route) }
                )
                QuickActionItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.RestaurantMenu,
                    label = "Meal Plan",
                    description = "View your plan",
                    onClick = { navController.navigate(Screen.MealPlan.route) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                QuickActionItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.BubbleChart,
                    label = "AI Tips",
                    description = "Get recommendations",
                    onClick = { navController.navigate(Screen.AiTips.route) }
                )
                QuickActionItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.History,
                    label = "History",
                    description = "View past logs",
                    onClick = { navController.navigate(Screen.History.route) }
                )
            }
        }
    }
}

@Composable
fun QuickActionItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryGreen)
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DailyTipCardFigma(tip: String) {
    val isDark = LocalDarkTheme.current
    val tipBrush = if (isDark) {
        Brush.horizontalGradient(listOf(Color(0xFF4C1D95).copy(alpha=0.3f), Color(0xFF5B21B6).copy(alpha=0.3f)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFF3E8FF), Color(0xFFEDE9FE)))
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(tipBrush)
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if(isDark) Color(0xFF4C1D95) else Color(0xFFF3E8FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = if(isDark) Color(0xFFA5B4FC) else Color(0xFF7C3AED),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "Daily Tip",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = if(isDark) Color(0xFFC4B5FD) else Color(0xFF4C1D95)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        tip,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
