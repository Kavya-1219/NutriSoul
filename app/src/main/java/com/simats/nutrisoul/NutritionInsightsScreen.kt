package com.simats.nutrisoul

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionInsightsScreen(navController: NavController) {
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
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .verticalScroll(rememberScrollState())
            ) {
                Header()
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WeeklyConsistencyCard()
                    AverageDailyIntakeCard()
                    MacroDistributionCard()
                    InsightsCard()
                }
            }
        }
    )
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
                shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                "Your 7-day performance",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun WeeklyConsistencyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Weekly Consistency", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFFC107))
            }
            Spacer(Modifier.height(20.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(
                    progress = 0.14f,
                    modifier = Modifier.size(100.dp),
                    strokeWidth = 8.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("14%", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("Logged", color = Color.Gray)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("1 out of 7 days logged this week", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun AverageDailyIntakeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Average Daily Intake", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF6A1B9A))
            }
            Spacer(Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Calories", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray)
                        Text("Under Target", color = Color.Gray)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("41 / 1405 kcal", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = 0.03f, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)))
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MacroChip("Protein", "1g", "10%", Color(0xFFE3F2FD), Color(0xFF1976D2))
                MacroChip("Carbs", "10g", "98%", Color(0xFFFFF3E0), Color(0xFFFFA000))
                MacroChip("Fats", "0g", "0%", Color(0xFFFBE9E7), Color(0xFFD84315))
            }
        }
    }
}

@Composable
private fun MacroDistributionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Macro Distribution", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Spacer(Modifier.height(20.dp))
            MacroDistributionItem("Protein", "10%", 0.10f, "Recommended: 15-30%")
            Spacer(Modifier.height(16.dp))
            MacroDistributionItem("Carbohydrates", "98%", 0.98f, "Recommended: 45-65%")
            Spacer(Modifier.height(16.dp))
            MacroDistributionItem("Fats", "0%", 0.0f, "Recommended: 20-35%")
        }
    }
}

@Composable
private fun MacroDistributionItem(name: String, percentage: String, progress: Float, recommended: String) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, fontWeight = FontWeight.SemiBold)
            Text(percentage, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)))
        Spacer(Modifier.height(4.dp))
        Text(recommended, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun InsightsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.padding(start = 16.dp, end = 16.dp))
                Text("Insights", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text("• Try to log meals more consistently for better insights")
            Spacer(Modifier.height(8.dp))
            Text("• Consider increasing protein intake")
        }
    }
}

@Composable
private fun MacroChip(name: String, value: String, percentage: String, backgroundColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(name, fontSize = 12.sp, color = textColor)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(percentage, fontSize = 12.sp, color = textColor)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NutritionInsightsScreenPreview() {
    NutritionInsightsScreen(rememberNavController())
}
