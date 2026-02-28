package com.simats.nutrisoul

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.simats.nutrisoul.data.User
import com.simats.nutrisoul.data.UserViewModel
import com.simats.nutrisoul.ui.DailyTotalsUi
import com.simats.nutrisoul.ui.theme.PrimaryGreen
import java.util.Calendar

@Composable
fun HomeScreen(navController: NavController, userViewModel: UserViewModel, homeViewModel: HomeViewModel = hiltViewModel()) {
    val user by userViewModel.user.collectAsStateWithLifecycle()
    val isLoading by userViewModel.isLoading.collectAsStateWithLifecycle()
    val totals by homeViewModel.todayTotals.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

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
            if (isLoading) {
                CircularProgressIndicator(color = PrimaryGreen)
            } else if (user != null) {
                HomeContent(navController, user!!, totals, homeUiState, userViewModel)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error loading user data.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { userViewModel.retryLoadUserData() }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
fun HomeContent(
    navController: NavController,
    user: User,
    totals: DailyTotalsUi,
    homeUiState: HomeUiState,
    userViewModel: UserViewModel
) {
    val targetCalories = when (user.goal) {
        "Weight Loss" -> user.bmr - 500
        "Gain Muscle" -> user.bmr + 500
        else -> user.bmr
    }
    val profilePictureUri by userViewModel.profilePictureUri.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Header(user.name, profilePictureUri)
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            DailyCalorieGoalCard(targetCalories, totals)
            WeightProgressCard(user.currentWeight, user.targetWeight, user.goal)
            TodayActivityCard(navController, user)
            QuickActionsCard(navController)
            DailyTipCard(homeUiState.dailyTip)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun Header(name: String, profilePictureUri: Uri?) {
    val timeOfDay = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryGreen,
                        Color(0xFF66BB6A)
                    )
                ),
                shape = RoundedCornerShape(
                    bottomStart = 40.dp,
                    bottomEnd = 40.dp
                )
            )
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    timeOfDay,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )
                Text(
                    name,
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                if (profilePictureUri != null) {
                    AsyncImage(
                        model = profilePictureUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("👋", fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
fun DailyCalorieGoalCard(targetCalories: Int, totals: DailyTotalsUi) {
    val consumed = totals.calories
    val goal = targetCalories
    val remaining = goal - consumed

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Daily Calorie Goal",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Outlined.LocalFireDepartment,
                    contentDescription = null,
                    tint = PrimaryGreen 
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold)) {
                        append(consumed.toInt().toString())
                    }
                    withStyle(SpanStyle(fontSize = 22.sp, color = Color.Gray)) {
                        append(" / $goal")
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = (consumed.toFloat() / goal).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(10.dp)),
                color = PrimaryGreen,
                trackColor = Color(0xFFE0E0E0)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Remaining: ${remaining.toInt()} kcal",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun WeightProgressCard(currentWeight: Float, targetWeight: Float, goal: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Weight Progress",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.BarChart, contentDescription = null, tint = PrimaryGreen)
            }

            Spacer(Modifier.height(20.dp))

            val isMaintainOrGain = goal.equals("Maintain Weight", ignoreCase = true) ||
                    goal.equals("Gain Muscle", ignoreCase = true)

            if (isMaintainOrGain) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    WeightBox(
                        label = "Current Weight",
                        value = "$currentWeight kg",
                        valueColor = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        WeightBox(
                            label = "Current",
                            value = "$currentWeight kg",
                            valueColor = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                        WeightBox(
                            label = "Target",
                            value = "$targetWeight kg",
                            valueColor = PrimaryGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    val weightToGoal = if (goal.equals("Weight Loss", ignoreCase = true)) {
                        currentWeight - targetWeight
                    } else {
                        targetWeight - currentWeight
                    }

                    if (targetWeight > 0 && weightToGoal != 0.0f) {
                        Text(
                            text = "${String.format("%.1f", weightToGoal)} kg to go",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeightBox(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label)
            Text(
                value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}


@Composable
fun TodayActivityCard(navController: NavController, user: User) {

    Column {

        Text(
            "Today's Activity",
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

            ActivityItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.WaterDrop,
                label = "ml water",
                value = user.todaysWaterIntake.toString(),
                background = Color(0xFFE3F2FD),
                iconColor = Color(0xFF42A5F5),
                onClick = { navController.navigate(Screen.WaterTracking.route) }
            )

            ActivityItem(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                label = "steps",
                value = user.todaysSteps.toString(),
                background = Color(0xFFF3E5F5),
                iconColor = Color(0xFFBA68C8),
                onClick = { navController.navigate(Screen.StepsTracking.route) }
            )
        }
    }
}

@Composable
fun ActivityItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    background: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = value,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
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
            fontSize = 18.sp,
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
fun DailyTipCard(tip: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(20.dp)) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Color(0xFF42A5F5),
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "Daily Tip",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    tip,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
    }
}
