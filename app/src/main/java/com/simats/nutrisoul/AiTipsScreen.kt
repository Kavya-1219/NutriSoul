package com.simats.nutrisoul

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.simats.nutrisoul.data.UserViewModel
import kotlin.math.roundToInt

val healthTips = mapOf(
    "Diabetes" to "Focus on high-fiber, low-GI foods",
    "PCOS" to "Include anti-inflammatory foods",
    "Thyroid Issues" to "Ensure adequate iodine and selenium intake",
    "High Blood Pressure" to "Limit sodium and increase potassium-rich foods",
    "Low Blood Pressure" to "Stay hydrated and consider small, frequent meals",
    "High Cholesterol" to "Increase soluble fiber and healthy fats",
    "Digestive Issues" to "Include probiotics and fermented foods",
    "Anemia" to "Boost iron intake with vitamin C-rich foods",
    "Food Allergies" to "Carefully read food labels and avoid cross-contamination"
)

@Composable
fun AiTipsScreen(navController: NavController, userViewModel: UserViewModel) {
    val user by userViewModel.user.collectAsStateWithLifecycle()

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF8E24AA), Color(0xFFD81B60))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = "AI",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Recommendations",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Body
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Personalized insights for",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 18.sp
                )
                Text(
                    text = user?.name ?: "k",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Goal Card
                GoalCard(user?.goal ?: "Lose Weight")

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Your Personalized Plan",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Tip Cards
                TipCard(
                    icon = Icons.Default.TrendingUp,
                    iconBgColor = Color(0xFFE3F2FD),
                    iconTint = Color(0xFF2962FF),
                    title = "Calorie Deficit Strategy",
                    content = "Aim for a deficit of 500 kcal/day (currently targeting ${user?.targetCalories ?: 1405} kcal). This will help you lose ~0.5kg per week safely."
                )
                TipCard(
                    icon = Icons.Default.Restaurant,
                    iconBgColor = Color(0xFFE8F5E9),
                    iconTint = Color(0xFF00C853),
                    title = "Protein Intake",
                    content = "Increase protein to 25-30% of calories (${((user?.targetCalories ?: 0) * 0.25f / 4f).roundToInt()}g daily) to preserve muscle mass during weight loss."
                )
                TipCard(
                    icon = Icons.Default.Schedule,
                    iconBgColor = Color(0xFFFFF3E0),
                    iconTint = Color(0xFFFF6D00),
                    title = "Meal Timing",
                    content = "Try eating smaller, frequent meals (4-5 times/day) to keep metabolism active and reduce hunger."
                )
                
                user?.healthConditions?.forEach { condition ->
                    healthTips[condition]?.let {
                        TipCard(
                            icon = Icons.Default.FavoriteBorder,
                            iconBgColor = Color(0xFFFFEBEE),
                            iconTint = Color(0xFFD32F2F),
                            title = "Health Tip: $condition",
                            content = it
                        )
                    }
                }

                val waterIntake = ((user?.currentWeight ?: 0f) * 30).roundToInt()
                val waterGlasses = (waterIntake / 250f).roundToInt()
                TipCard(
                    icon = Icons.Default.LocalDrink,
                    iconBgColor = Color(0xFFE3F2FD),
                    iconTint = Color(0xFF2962FF),
                    title = "Hydration Goal",
                    content = "Drink at least ${waterIntake}ml (${waterGlasses} glasses) of water daily based on your body weight."
                )
                ProTipsCard()
                NoteCard()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun GoalCard(goal: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFD81B60), Color(0xFF8E24AA))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.TrackChanges,
                    contentDescription = "Goal",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Your Goal", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Text(goal, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "These recommendations are tailored to help you achieve your goal safely and effectively.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}


@Composable
private fun TipCard(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    content: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = iconBgColor.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = content, fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun ProTipsCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFC107)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Pro Tips",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Pro Tips", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(12.dp))
            val tips = listOf(
                "Log your meals consistently for better insights",
                "Track your weight weekly, not daily",
                "Focus on progress, not perfection",
                "Get 7-9 hours of quality sleep",
                "Manage stress through meditation or yoga"
            )
            tips.forEach { tip ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                    Text("• ", color = Color.DarkGray)
                    Text(tip, fontSize = 14.sp, color = Color.DarkGray)
                }
            }
        }
    }
}

@Composable
fun NoteCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD).copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = "Note",
                tint = Color(0xFF2962FF),
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Note: These are AI-generated recommendations based on your profile. For medical conditions or specific health concerns, please consult a healthcare professional or registered dietitian.",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}
