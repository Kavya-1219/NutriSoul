package com.simats.nutrisoul

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* 
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.simats.nutrisoul.data.health.HealthConnectManager
import com.simats.nutrisoul.ui.steps.HealthConnectStatus
import com.simats.nutrisoul.ui.steps.StepsScreenEvent
import com.simats.nutrisoul.ui.steps.StepsUiState
import com.simats.nutrisoul.ui.steps.StepsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepsTrackingScreen(
    navController: NavController,
    viewModel: StepsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val healthConnectManager = HealthConnectManager(context)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = healthConnectManager.requestPermissionsActivityContract()
    ) { grantedPermissions ->
        if (HealthConnectManager.PERMISSIONS.containsAll(grantedPermissions)) {
            viewModel.onEvent(StepsScreenEvent.OnPermissionResult(true))
        } else {
            viewModel.onEvent(StepsScreenEvent.OnPermissionResult(false))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Steps Tracking") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF0FDF4)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            HeaderSection()

            if (!uiState.hasPermissions || uiState.healthConnectStatus != HealthConnectStatus.Installed) {
                PermissionsCard(
                    status = uiState.healthConnectStatus,
                    onGrantClick = { permissionLauncher.launch(HealthConnectManager.PERMISSIONS) },
                    onInstallClick = { healthConnectManager.installHealthConnect() }
                )
            } else {
                StepsContent(
                    uiState = uiState,
                    onGoalSelected = { goal -> viewModel.onEvent(StepsScreenEvent.OnGoalSelected(goal)) },
                    onAddStepsClicked = { /* TODO: Implement Manual Add Steps Dialog */ }
                )
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                brush = Brush.verticalGradient(colors = listOf(Color(0xFF10B981), Color(0xFF059669))),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Steps Tracking",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Every step brings you closer!",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun StepsContent(
    uiState: StepsUiState,
    onGoalSelected: (Int) -> Unit,
    onAddStepsClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .offset(y = (-50).dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProgressCard(uiState.todaySteps, uiState.stepsGoal)
        StatsCard(uiState.caloriesBurned, uiState.distanceKm, uiState.weeklyAverage)
        DailyGoalCard(uiState.stepsGoal, onGoalSelected)
        
        Button(
            onClick = onAddStepsClicked,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Steps")
            Spacer(Modifier.width(8.dp))
            Text("Add Steps", fontSize = 16.sp)
        }
        
        TipsCard()
    }
}


@Composable
fun ProgressCard(steps: Long, goal: Int) {
    val progress = if (goal > 0) (steps.toFloat() / goal).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000))
    
    val achievement = when {
        steps >= 15000 -> "Super Active"
        steps >= 10000 -> "Very Active"
        steps >= 7500 -> "Active"
        else -> "Getting Started"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 50f
                    drawArc(
                        color = Color(0xFFD1FAE5),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        brush = Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                        startAngle = -90f,
                        sweepAngle = 360 * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    //Icon(painterResource(R.drawable.ic_footsteps), contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(40.dp))
                    Text(
                        steps.toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text("steps", color = Color.Gray)
                    Text(
                        "${(animatedProgress * 100).toInt()}%",
                        color = Color(0xFF059669),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                text = achievement,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0FDF4))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Text(
                text = "Let's get moving today!",
                color = Color(0xFF059669),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0FDF4))
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatsCard(calories: Int, distance: Double, avgSteps: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatItem(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.LocalFireDepartment,
            value = calories.toString(),
            label = "kcal",
            iconColor = Color(0xFFEF4444)
        )
        /*StatItem(
            modifier = Modifier.weight(1f),
            icon = painterResource(id = R.drawable.ic_target),
            value = String.format("%.2f", distance),
            label = "km",
            iconColor = Color(0xFF3B82F6)
        )*/
        /*StatItem(
            modifier = Modifier.weight(1f),
            icon = painterResource(id = R.drawable.ic_trending_up),
            value = avgSteps.toString(),
            label = "7-day avg",
            iconColor = Color(0xFF8B5CF6)
        )*/
    }
}

@Composable
fun DailyGoalCard(currentGoal: Int, onGoalSelected: (Int) -> Unit) {
    val goals = listOf(5000, 8000, 10000, 12000, 15000)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Daily Goal", color = Color.Gray, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            goals.chunked(3).forEach { rowGoals ->
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowGoals.forEach { goal ->
                        Button(
                            onClick = { onGoalSelected(goal) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentGoal == goal) Color(0xFF10B981) else Color.White,
                                contentColor = if (currentGoal == goal) Color.White else Color.Black
                            ),
                            elevation = ButtonDefaults.buttonElevation(if (currentGoal == goal) 2.dp else 0.dp)
                        ) {
                            Text("${goal / 1000}k")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("💡 Tips to Increase Steps", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            listOf(
                "Take the stairs instead of elevators",
                "Park farther away from entrances",
                "Take short walking breaks every hour",
                "Walk while talking on the phone"
            ).forEach { tip ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👟", modifier = Modifier.padding(end = 8.dp))
                    Text(tip, fontSize = 14.sp)
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun PermissionsCard(
    status: HealthConnectStatus,
    onGrantClick: () -> Unit,
    onInstallClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (status == HealthConnectStatus.NotInstalled) {
                Text("Health Connect Not Installed", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text("To track your steps automatically, please install the Health Connect app from Google.", textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onInstallClick) { Text("Install Now") }
            } else {
                Text("Permission Required", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text("NutriSoul needs your permission to read your daily steps data from Health Connect.", textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onGrantClick) { Text("Grant Permission") }
            }
        }
    }
}

// NOTE: You will need to add these drawable resources `ic_footsteps`, `ic_target`, and `ic_trending_up`
// to your `res/drawable` folder. You can use any vector assets for these.
@Composable
fun StatItem(modifier: Modifier, icon: Any, value: String, label: String, iconColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (icon) {
                is ImageVector -> Icon(icon, contentDescription = null, tint = iconColor)
                //is Int -> Icon(painterResource(id = icon), contentDescription = null, tint = iconColor)
            }
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(label, color = Color.Gray, fontSize = 12.sp)
        }
    }
}