package com.simats.nutrisoul

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionInsightsScreen(
    navController: NavController,
    viewModel: NutritionInsightsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nutrition Insights") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6A1B9A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Header()

            when (state) {
                NutritionInsightsUiState.Loading -> LoadingBody()
                NutritionInsightsUiState.Empty -> EmptyBody()
                is NutritionInsightsUiState.Success -> {
                    val data = (state as NutritionInsightsUiState.Success).data
                    Body(data)
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF6A1B9A), Color(0xFFAB47BC))
                ),
                shape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)
            )
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Column {
            Text(
                text = "Your 7-day performance",
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Track your nutrition trends",
                color = Color.White.copy(alpha = 0.80f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun LoadingBody() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(18.dp))
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text("Loading insights…", color = Color.Gray)
    }
}

@Composable
private fun EmptyBody() {
    Column(modifier = Modifier.padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text("No Data Yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Start logging your meals to see personalized nutrition insights and track your progress over time.",
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                ) {
                    Text(
                        "💡 Tip: Log meals for at least 3–4 days to get meaningful insights!",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF1E40AF),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun Body(data: NutritionInsightsData) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WeeklyConsistencyCard(data)
        AverageDailyIntakeCard(data)
        MacroDistributionCard(data)
        InsightsCard(data)
    }
}

@Composable
private fun WeeklyConsistencyCard(data: NutritionInsightsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Weekly Consistency", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = Color(0xFFFFC107))
            }

            Spacer(Modifier.height(18.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(
                    progress = data.weeklyConsistency.coerceIn(0f, 1f),
                    modifier = Modifier.size(112.dp),
                    strokeWidth = 10.dp,
                    color = Color(0xFF10B981),
                    trackColor = Color(0xFFE5E7EB)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${data.consistencyPercent}%", fontWeight = FontWeight.Bold, fontSize = 26.sp)
                    Text("Logged", color = Color.Gray, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                "${data.daysLogged} out of ${data.totalDays} days logged this week",
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun AverageDailyIntakeCard(data: NutritionInsightsData) {
    val status = data.calorieStatus

    val statusBg = when (status.tone) {
        StatusTone.GOOD -> Color(0xFFECFDF5)
        StatusTone.OK -> Color(0xFFEFF6FF)
        StatusTone.WARN -> Color(0xFFFFF7ED)
        StatusTone.INFO -> Color(0xFFFFFBEB)
        StatusTone.NEUTRAL -> Color(0xFFF3F4F6)
    }

    val barColor = when (status.tone) {
        StatusTone.GOOD, StatusTone.OK -> Color(0xFF10B981)
        StatusTone.WARN -> Color(0xFFF97316)
        StatusTone.INFO -> Color(0xFFF59E0B)
        StatusTone.NEUTRAL -> Color(0xFF9CA3AF)
    }

    val progress = if (data.targetCalories > 0)
        (data.averageCalories / data.targetCalories.toFloat()).coerceIn(0f, 1f)
    else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Average Daily Intake", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color(0xFF6A1B9A))
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = statusBg)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Calories", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text("${status.emoji} ${status.label}", color = Color(0xFF374151), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${data.averageCalories}", fontWeight = FontWeight.Bold, fontSize = 26.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("/ ${data.targetCalories} kcal", color = Color.Gray, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(6.dp)),
                        color = barColor,
                        trackColor = Color(0xFFE5E7EB)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MacroChip(
                    modifier = Modifier.weight(1f),
                    name = "Protein",
                    value = String.format("%.1fg", data.averageProtein),
                    percentage = "${data.proteinPercentage}%",
                    bg = Color(0xFFEFF6FF),
                    fg = Color(0xFF2563EB)
                )
                MacroChip(
                    modifier = Modifier.weight(1f),
                    name = "Carbs",
                    value = String.format("%.1fg", data.averageCarbs),
                    percentage = "${data.carbsPercentage}%",
                    bg = Color(0xFFFFF7ED),
                    fg = Color(0xFFEA580C)
                )
                MacroChip(
                    modifier = Modifier.weight(1f),
                    name = "Fats",
                    value = String.format("%.1fg", data.averageFats),
                    percentage = "${data.fatsPercentage}%",
                    bg = Color(0xFFFFFBEB),
                    fg = Color(0xFFD97706)
                )
            }
        }
    }
}

@Composable
private fun MacroDistributionCard(data: NutritionInsightsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Macro Distribution", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))

            MacroDistributionItem("Protein", data.proteinPercentage, "Recommended: 15–30%")
            Spacer(Modifier.height(14.dp))
            MacroDistributionItem("Carbohydrates", data.carbsPercentage, "Recommended: 45–65%")
            Spacer(Modifier.height(14.dp))
            MacroDistributionItem("Fats", data.fatsPercentage, "Recommended: 20–35%")
        }
    }
}

@Composable
private fun MacroDistributionItem(name: String, pct: Int, recommended: String) {
    val progress = (pct / 100f).coerceIn(0f, 1f)
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, fontWeight = FontWeight.SemiBold)
            Text("$pct%", color = Color(0xFF6A1B9A), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(6.dp)),
            color = Color(0xFF6A1B9A),
            trackColor = Color(0xFFE5E7EB)
        )
        Spacer(Modifier.height(4.dp))
        Text(recommended, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun InsightsCard(data: NutritionInsightsData) {
    val insights = buildList {
        if (data.consistencyPercent >= 80) add("✓ Great job staying consistent with logging!")
        if (data.consistencyPercent < 50) add("• Try to log meals more consistently for better insights")
        if (data.calorieStatus.label == "Excellent") add("✓ Your calorie intake is perfectly on target")

        val proteinOk = data.proteinPercentage in 15..30
        if (proteinOk) add("✓ Protein intake is in the healthy range")
        if (!proteinOk) add("• Consider increasing protein intake")

        if (data.fatsPercentage > 35) add("• Fat intake is a bit high - try lean proteins")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.TrackChanges, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Text("Insights", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            }

            Spacer(Modifier.height(12.dp))
            insights.forEachIndexed { i, line ->
                Text(line)
                if (i != insights.lastIndex) Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun MacroChip(
    modifier: Modifier,
    name: String,
    value: String,
    percentage: String,
    bg: Color,
    fg: Color
) {
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(name, fontSize = 12.sp, color = fg)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = fg)
            Text(percentage, fontSize = 12.sp, color = fg)
        }
    }
}
