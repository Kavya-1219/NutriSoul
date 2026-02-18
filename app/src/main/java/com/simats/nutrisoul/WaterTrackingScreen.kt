package com.simats.nutrisoul

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.simats.nutrisoul.data.UserViewModel

@Composable
fun WaterTrackingScreen(navController: NavController, userViewModel: UserViewModel) {
    val user by userViewModel.user.collectAsStateWithLifecycle()
    val dailyGoal = 2275
    var selectedGlassSize by remember { mutableStateOf(250) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00B2FF))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            WaterTrackingHeader(navController)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WaterProgress(user?.todaysWaterIntake ?: 0, dailyGoal)
                TodayProgress(user?.todaysWaterIntake ?: 0)
                HydrationJourneyMessage()
                GlassSizeSelector(selectedGlassSize) { selectedGlassSize = it }
                WaterActionButtons(
                    onAdd = {
                        val updatedUser = user?.copy(todaysWaterIntake = (user?.todaysWaterIntake ?: 0) + selectedGlassSize)
                        if (updatedUser != null) {
                            userViewModel.updateUser(updatedUser)
                        }
                    },
                    onRemove = {
                        val updatedUser = user?.copy(todaysWaterIntake = ((user?.todaysWaterIntake ?: 0) - selectedGlassSize).coerceAtLeast(0))
                        if (updatedUser != null) {
                            userViewModel.updateUser(updatedUser)
                        }
                    },
                    glassSize = selectedGlassSize
                )
                WaterStats(dailyGoal)
                HydrationBenefits()
            }
        }
    }
}

@Composable
private fun WaterTrackingHeader(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = "Water Tracking",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Stay hydrated, stay healthy!",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun WaterProgress(waterIntake: Int, dailyGoal: Int) {
    val progress = if (dailyGoal > 0) waterIntake.toFloat() / dailyGoal else 0f
    val percentage = (progress * 100).toInt()

    Box(
        modifier = Modifier
            .size(220.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 16.dp,
            color = Color(0xFF00B2FF),
            trackColor = Color(0xFFE0E0E0).copy(alpha = 0.5f)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.WaterDrop,
                contentDescription = null,
                tint = Color(0xFF00B2FF),
                modifier = Modifier.size(40.dp)
            )
            Text(
                "$waterIntake",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Text("ml / $dailyGoal", color = Color.Gray)
            Text("$percentage %", color = Color.Gray)
        }
    }
}

@Composable
private fun TodayProgress(waterIntake: Int) {
    val glasses = waterIntake / 250
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Today's Progress", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Text("$glasses / 10 glasses", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00B2FF))
        Text("(250ml per glass)", color = Color.Gray)
    }
}

@Composable
private fun HydrationJourneyMessage() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFC107))
            Spacer(Modifier.width(8.dp))
            Text("Start your hydration journey today!", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun GlassSizeSelector(selectedGlass: Int, onGlassSizeSelected: (Int) -> Unit) {
    val glassSizes = listOf(200, 250, 300, 350, 400, 500)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Glass Size", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            glassSizes.take(3).forEach { size ->
                GlassSizeButton(size, selectedGlass, onGlassSizeSelected)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            glassSizes.takeLast(3).forEach { size ->
                GlassSizeButton(size, selectedGlass, onGlassSizeSelected)
            }
        }
    }
}

@Composable
private fun GlassSizeButton(size: Int, selectedGlass: Int, onGlassSizeSelected: (Int) -> Unit) {
    Button(
        onClick = { onGlassSizeSelected(size) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selectedGlass == size) Color(0xFF00B2FF) else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = if (selectedGlass != size) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null,
        border = if (selectedGlass != size) BorderStroke(1.dp, Color.LightGray.copy(alpha=0.5f)) else null
    ) {
        Text("${size}ml", color = if (selectedGlass == size) Color.White else Color.Black)
    }
}

@Composable
private fun WaterActionButtons(onAdd: () -> Unit, onRemove: () -> Unit, glassSize: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Button(
            onClick = onRemove,
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha=0.5f)),
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Remove")
            Text("Remove")
        }
        Spacer(Modifier.width(16.dp))
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B2FF)),
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
            Text("Add ${glassSize}ml")
        }
    }
}

@Composable
private fun WaterStats(dailyGoal: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Card(modifier = Modifier.weight(1f)) {
            Column(Modifier.padding(16.dp)) {
                Icon(Icons.Default.TrackChanges, contentDescription = null, tint = Color(0xFF00C853))
                Text("Daily Goal", fontWeight = FontWeight.Bold)
                Text("$dailyGoal ml", color = Color(0xFF00C853), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("10 glasses")
            }
        }
        Spacer(Modifier.width(16.dp))
        Card(modifier = Modifier.weight(1f)) {
            Column(Modifier.padding(16.dp)) {
                Icon(Icons.Default.BarChart, contentDescription = null, tint = Color(0xFF00B2FF))
                Text("7-Day Avg", fontWeight = FontWeight.Bold)
                Text("0ml", color = Color(0xFF00B2FF), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("0% of goal")
            }
        }
    }
}

@Composable
private fun HydrationBenefits() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha=0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Benefits of Hydration", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))
            BenefitItem("Boosts metabolism - Helps burn more calories")
            BenefitItem("Reduces appetite - Helps with weight management")
            BenefitItem("Improves skin - Keeps skin hydrated and glowing")
            BenefitItem("Better digestion - Aids nutrient absorption")
        }
    }
}

@Composable
private fun BenefitItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF00B2FF), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.Gray, fontSize = 14.sp)
    }
}
