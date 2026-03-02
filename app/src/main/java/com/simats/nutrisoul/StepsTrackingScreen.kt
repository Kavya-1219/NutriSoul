package com.simats.nutrisoul

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.simats.nutrisoul.ui.steps.AddStepsModal
import com.simats.nutrisoul.ui.steps.StepsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepsTrackingScreen(
    navController: NavController,
    viewModel: StepsViewModel = hiltViewModel()
) {
    val todaySteps by viewModel.todaySteps.collectAsStateWithLifecycle()
    val autoEnabled by viewModel.autoEnabled.collectAsStateWithLifecycle()
    var showAddSteps by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setAutoTracking(true)
        } else {
            viewModel.setAutoTracking(false)
            Toast.makeText(context, "Permission required to enable auto tracking", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0FDF4))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            HeaderSection(
                onBack = { navController.popBackStack() }
            )

            StepsContent(
                todaySteps = todaySteps,
                autoEnabled = autoEnabled,
                onToggleAutoTracking = { checked ->
                    if (checked) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                        } else {
                            viewModel.setAutoTracking(true)
                        }
                    } else {
                        viewModel.setAutoTracking(false)
                    }
                },
                onAddStepsClicked = { showAddSteps = true },
            )

            Spacer(Modifier.height(30.dp))
        }
        if (showAddSteps) {
            AddStepsModal(
                onDismiss = { showAddSteps = false },
                onAddSteps = { input ->
                    val v = input.toIntOrNull() ?: 0
                    viewModel.addManualSteps(v)
                    showAddSteps = false
                }
            )
        }
    }
}

@Composable
private fun HeaderSection(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF10B981), Color(0xFF059669))
                ),
                shape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)
            )
            .padding(start = 16.dp, end = 16.dp, top = 18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(Modifier.width(6.dp))
                Text("👣", fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))

                Text(
                    "Steps Tracking",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Every step brings you closer!",
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun StepsContent(
    todaySteps: Int,
    autoEnabled: Boolean,
    onToggleAutoTracking: (Boolean) -> Unit,
    onAddStepsClicked: () -> Unit,
) {
    val calories = (todaySteps * 0.04).toInt()
    val distance = todaySteps * 0.000762

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .offset(y = (-70).dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        ProgressCard(todaySteps.toLong(), 10000)

        StatsCard(calories, distance, 0)

        AutoTrackingCard(
            enabled = autoEnabled,
            onToggle = onToggleAutoTracking,
            disabled = false
        )

        Button(
            onClick = onAddStepsClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(10.dp))
                    Text("Add Steps", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        TipsCard()
    }
}

@Composable
fun ProgressCard(steps: Long, goal: Int) {
    val progress = if (goal > 0) (steps.toFloat() / goal).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000), label = "")
    val density = LocalDensity.current

    val achievement = when {
        steps >= 15000 -> "Super Active"
        steps >= 10000 -> "Very Active"
        steps >= 7500 -> "Active"
        else -> "Getting Started"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3FFF8))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = with(density) { 16.dp.toPx() }
                    drawArc(
                        color = Color(0xFFD1FAE5),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                        startAngle = -90f,
                        sweepAngle = 360 * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp))
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏅", fontSize = 18.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(achievement, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFECFDF5))
                    .border(1.5.dp, Color(0xFFBBF7D0), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🌟 Let's get moving today!",
                    color = Color(0xFF065F46),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
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
            iconColor = Color(0xFFFB6A2D)
        )
        StatItem(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.TrackChanges,
            value = String.format("%.2f", distance),
            label = "km",
            iconColor = Color(0xFF2563EB)
        )
        StatItem(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.TrendingUp,
            value = avgSteps.toString(),
            label = "7-day avg",
            iconColor = Color(0xFF7C3AED)
        )
    }
}

@Composable
private fun AutoTrackingCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    disabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Auto Tracking", fontWeight = FontWeight.SemiBold)
                Text(
                    if (disabled) "Grant permission to enable"
                    else if (enabled) "Tracking is ON"
                    else "Tracking is OFF",
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = {
                    onToggle(it)
                },
                enabled = !disabled
            )
        }
    }
}


@Composable
fun StatItem(modifier: Modifier = Modifier, icon: ImageVector, value: String, label: String, iconColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(label, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun TipsCard() {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("💡", modifier = Modifier.padding(end = 16.dp), fontSize = 24.sp)
            Column {
                Text("Tip of the Day", fontWeight = FontWeight.Bold, color = Color(0xFF004D40))
                Text("Take the stairs instead of the elevator to easily add more steps to your day!", fontSize = 14.sp, color = Color(0xFF00796B))
            }
        }
    }
}
