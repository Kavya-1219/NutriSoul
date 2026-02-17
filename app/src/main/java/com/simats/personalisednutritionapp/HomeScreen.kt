package com.simats.personalisednutritionapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* 
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.simats.personalisednutritionapp.data.UserViewModel
import com.simats.personalisednutritionapp.ui.theme.PrimaryGreen
import java.util.Calendar

@Composable
fun HomeScreen(navController: NavController, userViewModel: UserViewModel) {
    val user by userViewModel.user.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(onItemSelected = {}) }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HomeContent(navController, userViewModel)
        }
    }
}

@Composable
fun HomeContent(navController: NavController, userViewModel: UserViewModel) {
    val user by userViewModel.user.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Header(user?.name ?: "User")
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            DailyCalorieGoalCard(user?.targetCalories ?: 0.0)
            WeightProgressCard(user?.currentWeight ?: 0.0, user?.targetWeight ?: 0.0, user?.goal ?: "")
            TodayActivityCard(navController)
            QuickActionsCard(navController)
            DailyTipCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun Header(name: String) {
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
                Text("👋", fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun DailyCalorieGoalCard(targetCalories: Double) {
    val consumed = 0 // This will be updated later from the database
    val goal = targetCalories.toInt()
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
                        append(consumed.toString())
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
                "Remaining: $remaining kcal",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun WeightProgressCard(currentWeight: Double, targetWeight: Double, goal: String) {
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

            if (goal == "Maintain Weight" || goal == "Gain Muscle") {
                Row(modifier = Modifier.fillMaxWidth()) {
                    WeightBox("Current Weight", "$currentWeight kg", MaterialTheme.colorScheme.onBackground)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WeightBox("Current", "$currentWeight kg", MaterialTheme.colorScheme.onBackground)
                    WeightBox("Target", "$targetWeight kg", PrimaryGreen)
                }

                Spacer(Modifier.height(16.dp))

                val weightToGoal = if (goal == "Weight Loss") {
                    (currentWeight - targetWeight).toFloat()
                } else {
                    (targetWeight - currentWeight).toFloat()
                }

                Text(
                    text = "${String.format("%.1f", weightToGoal)} kg to go",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun RowScope.WeightBox(label: String, value: String, valueColor: Color) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
fun TodayActivityCard(navController: NavController) {

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
                value = "0", // Will be replaced with data from ViewModel
                background = Color(0xFFE3F2FD),
                iconColor = Color(0xFF42A5F5),
                onClick = { navController.navigate(Screen.WaterTracking.route) }
            )

            ActivityItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.DirectionsWalk,
                label = "steps",
                value = "0", // Will be replaced with data from ViewModel
                background = Color(0xFFE8F5E9),
                iconColor = Color(0xFF66BB6A),
                onClick = { navController.navigate(Screen.StepsTracking.route) }
            )
        }
    }
}

@Composable
fun RowScope.ActivityItem(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    background: Color,
    iconColor: Color,
    onClick: () -> Unit
) {

    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
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

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickActionItem("Log Food", Icons.Default.Add, Color(0xFF66BB6A)) { navController.navigate(Screen.LogFood.route) }
            QuickActionItem("Meal Plan", Icons.Default.CameraAlt, Color(0xFF42A5F5)) {}
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickActionItem("AI Tips", Icons.Default.Psychology, Color(0xFFAB47BC)) { navController.navigate(Screen.AiTips.route) }
            QuickActionItem("History", Icons.Default.History, Color(0xFFFF7043)) {}
        }
    }
}

@Composable
fun RowScope.QuickActionItem(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun DailyTipCard() {

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFAB47BC).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.TrackChanges, contentDescription = null, tint = Color(0xFFAB47BC))
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    "Daily Tip",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFAB47BC)
                )
                Text(
                    "You\'re doing great! Make sure to eat enough to meet your daily goals.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
