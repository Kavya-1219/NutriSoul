package com.simats.nutrisoul

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.simats.nutrisoul.data.UserViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * AI Recommendations Engine - Rule Set
 * In a real production environment, these would be fetched from a backend
 * that processes historical trends and metabolic data.
 */
private val healthTips = mapOf(
    "Diabetes" to "Focus on high-fiber, low-GI foods. Prefer whole grains, legumes, veggies, and avoid sugary drinks.",
    "PCOS" to "Include anti-inflammatory foods like nuts, seeds, fatty fish, and plenty of vegetables. Limit refined carbs.",
    "Thyroid Issues" to "Ensure adequate iodine and selenium intake. Include eggs, dairy, nuts, and balanced meals.",
    "High Blood Pressure" to "Limit sodium and include potassium-rich foods like bananas, spinach, coconut water, and dals.",
    "Low Blood Pressure" to "Stay hydrated and consider small, frequent meals. Include electrolytes if needed.",
    "High Cholesterol" to "Increase soluble fiber (oats, apples, beans) and healthy fats (olive oil, nuts).",
    "Digestive Issues" to "Include probiotics/fermented foods and gradually increase fiber. Stay hydrated.",
    "Anemia" to "Boost iron intake with vitamin C pairing (lemon, amla). Include leafy greens, legumes, and dates.",
    "Food Allergies" to "Read labels carefully and avoid cross-contamination. Prefer simple, home-cooked meals."
)

private data class Recommendation(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val cardBg: Color,
    val border: Color
)

@Composable
fun AiTipsScreen(navController: NavController, userViewModel: UserViewModel) {
    val userState by userViewModel.user.collectAsStateWithLifecycle()
    val user = userState

    // Mock Analytics for Production Ready status
    LaunchedEffect(Unit) {
        Log.d("Analytics", "AI_TIPS_SCREEN_OPENED")
    }

    val headerBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF8E24AA), Color(0xFFD81B60))
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Header background (Figma style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(headerBrush)
        )

        // Content container
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(8.dp))

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back),
                        tint = Color.White
                    )
                }
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = stringResource(R.string.ai_assistant_icon),
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.ai_tips_title),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Main surface (White rounded container)
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFF6F7FB),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                if (user == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                stringResource(R.string.analyzing_profile),
                                color = Color.DarkGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    val recommendations = remember(user) { buildRecommendations(user) }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 18.dp, bottom = 22.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.personalized_insights_for),
                                color = Color(0xFF5B6472),
                                fontSize = 14.sp
                            )
                            Text(
                                text = user.name.ifBlank { stringResource(R.string.default_user_name) },
                                color = Color(0xFF111827),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        item {
                            GoalCard(goal = user.goal.ifBlank { stringResource(R.string.default_goal) })
                        }

                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.your_personalized_plan),
                                color = Color(0xFF111827),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(recommendations) { rec ->
                            RecommendationCard(rec)
                        }

                        item {
                            ProTipsCard()
                        }

                        item {
                            NoteCard()
                        }
                    }
                }
            }
        }
    }
}

private fun buildRecommendations(user: com.simats.nutrisoul.data.User): List<Recommendation> {
    val list = mutableListOf<Recommendation>()

    val goal = user.goal.lowercase().trim()
    val targetCalories = user.targetCalories.takeIf { it > 0 } ?: 1400

    fun add(
        title: String,
        desc: String,
        icon: ImageVector,
        iconBg: Color,
        iconTint: Color,
        cardBg: Color,
        border: Color
    ) {
        list.add(Recommendation(title, desc, icon, iconBg, iconTint, cardBg, border))
    }

    // 1. BMI Calculation Logic (Safety: check height > 0)
    if (user.height > 0 && user.currentWeight > 0) {
        val heightM = user.height / 100f
        val bmi = user.currentWeight / (heightM * heightM)
        val bmiStatus = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Normal"
            bmi < 30 -> "Overweight"
            else -> "Obese"
        }
        add(
            "BMI Status: ${"%.1f".format(bmi)} ($bmiStatus)",
            "Your BMI is $bmiStatus. Based on current trends, " + when(bmiStatus) {
                "Underweight" -> "increasing calorie density is recommended to build healthy mass."
                "Normal" -> "you are in the optimal range. Focus on maintaining metabolic flexibility."
                "Overweight", "Obese" -> "a slight calorie deficit is recommended to reduce systemic inflammation."
                else -> ""
            },
            Icons.Default.Info,
            Color(0xFFE1F5FE),
            Color(0xFF0288D1),
            Color(0xFFF0F9FF),
            Color(0xFFBAE6FD)
        )
    }

    // 2. Goal & Weight Gap Analysis
    val weightDiff = user.targetWeight - user.currentWeight
    when {
        (goal.contains("lose") || weightDiff < -0.5f) && abs(weightDiff) > 0.5f -> {
            add(
                "Weight Management Strategy",
                "To reach your target, you need to lose ${abs(weightDiff).roundToInt()} kg. AI Suggestion: deficit around $targetCalories kcal with high protein to spare muscle.",
                Icons.AutoMirrored.Filled.TrendingDown,
                Color(0xFFE3F2FD),
                Color(0xFF2962FF),
                Color(0xFFEFF6FF),
                Color(0xFFBFDBFE)
            )
            val proteinG = (targetCalories * 0.25f / 4f).roundToInt()
            add(
                "Metabolic Support",
                "Target ~${proteinG}g of protein daily. This prevents metabolic slowdown while you're in a calorie deficit.",
                Icons.Default.Restaurant,
                Color(0xFFE8F5E9),
                Color(0xFF00C853),
                Color(0xFFF0FDF4),
                Color(0xFFBBF7D0)
            )
        }

        goal.contains("muscle") || goal.contains("gain") || weightDiff > 0.5f -> {
            add(
                "Growth & Recovery Plan",
                "Aiming for ${weightDiff.roundToInt().coerceAtLeast(0)} kg gain. AI Suggestion: consistent surplus (~$targetCalories kcal) paired with resistance training.",
                Icons.AutoMirrored.Filled.TrendingUp,
                Color(0xFFFFF3E0),
                Color(0xFFFF6D00),
                Color(0xFFFFFBEB),
                Color(0xFFFDE68A)
            )
            val proteinG = (user.currentWeight * 1.6f).roundToInt().coerceAtLeast(60)
            add(
                "Anabolic Environment",
                "Target ~1.6g/kg protein (~${proteinG}g/day) to maximize muscle protein synthesis. Spread across 4-5 meals.",
                Icons.Default.FitnessCenter,
                Color(0xFFF3E8FF),
                Color(0xFF7C3AED),
                Color(0xFFFAF5FF),
                Color(0xFFE9D5FF)
            )
        }

        else -> {
            add(
                "Maintenance & Optimization",
                "Stay around $targetCalories kcal/day. Focus on dietary diversity and consistent sleep to optimize recovery.",
                Icons.Default.TrackChanges,
                Color(0xFFE8F5E9),
                Color(0xFF00C853),
                Color(0xFFF0FDF4),
                Color(0xFFBBF7D0)
            )
        }
    }

    // 3. Activity & Lifestyle (Safety: handle blank/null)
    val activity = user.activityLevel.lowercase().trim()
    if (activity.isNotBlank()) {
        val activityDesc = when {
            activity.contains("sedentary") -> "Activity levels are low. Focus on NEAT (Non-Exercise Activity Thermogenesis) like standing or short walks."
            activity.contains("light") -> "Daily activity is good. Adding 2 days of strength training will boost your BMR."
            activity.contains("moderat") -> "Excellent activity level. Prioritize electrolyte balance and post-workout recovery."
            activity.contains("active") || activity.contains("very") -> "High volume detected. Ensure you are meeting carbohydrate targets for glycogen replenishment."
            else -> "Consistent movement supports your overall health and $goal goal."
        }
        add(
            "Lifestyle Optimization",
            activityDesc,
            Icons.AutoMirrored.Filled.DirectionsRun,
            Color(0xFFF1F8E9),
            Color(0xFF558B2F),
            Color(0xFFF7FDF4),
            Color(0xFFDCEDC8)
        )
    }

    // 4. Vitals & Medical Insights
    if (user.systolic > 140 || user.diastolic > 90) {
        add(
            "Vitals Alert: BP Management",
            "Recorded BP (${user.systolic}/${user.diastolic}) is elevated. AI Insight: Focus on the DASH diet (rich in potassium, calcium, and magnesium).",
            Icons.Default.Favorite,
            Color(0xFFFFEBEE),
            Color(0xFFD32F2F),
            Color(0xFFFFF1F2),
            Color(0xFFFECACA)
        )
    }

    if (user.cholesterolLevel.lowercase() == "high") {
        add(
            "Cardiovascular Health",
            "Prioritize phytosterols and omega-3 fatty acids. Limit saturated fats to <7% of total calories.",
            Icons.Default.MonitorHeart,
            Color(0xFFE0F2F1),
            Color(0xFF00796B),
            Color(0xFFF0FDFD),
            Color(0xFFB2DFDB)
        )
    }

    // Defensive check for healthConditions
    user.healthConditions.forEach { condition ->
        healthTips[condition]?.let { tip ->
            add(
                "Condition Management: $condition",
                tip,
                Icons.Default.FavoriteBorder,
                Color(0xFFFFEBEE),
                Color(0xFFD32F2F),
                Color(0xFFFFF1F2),
                Color(0xFFFECACA)
            )
        }
    }

    // 5. Hydration Optimization
    if (user.currentWeight > 0) {
        val waterMl = (user.currentWeight * 35f).roundToInt().coerceAtLeast(1500)
        val glasses = (waterMl / 250f).roundToInt().coerceAtLeast(6)
        add(
            "Fluid Balance",
            "Drink about ${waterMl}ml daily (~$glasses glasses). Hydration status directly impacts cognitive function and satiety.",
            Icons.Default.WaterDrop,
            Color(0xFFE3F2FD),
            Color(0xFF2962FF),
            Color(0xFFEFF6FF),
            Color(0xFFBFDBFE)
        )
    }

    // Cap recommendations for better UX and performance
    return list.take(7)
}

@Composable
private fun GoalCard(goal: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
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
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.your_goal_label), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF6B7280))
                Text(goal, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    stringResource(R.string.goal_card_description),
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
private fun RecommendationCard(rec: Recommendation) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = rec.cardBg),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, rec.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(rec.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(rec.icon, contentDescription = rec.title, tint = rec.iconTint)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(rec.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF111827))
                Spacer(modifier = Modifier.height(6.dp))
                Text(rec.description, fontSize = 13.sp, color = Color(0xFF374151), lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun ProTipsCard() {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFC107)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = stringResource(R.string.pro_tips_icon), tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.pro_tips_label), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF111827))
            }

            Spacer(modifier = Modifier.height(12.dp))

            val tips = listOf(
                stringResource(R.string.pro_tip_1),
                stringResource(R.string.pro_tip_2),
                stringResource(R.string.pro_tip_3),
                stringResource(R.string.pro_tip_4)
            )
            tips.forEach { tip ->
                Row(modifier = Modifier.padding(bottom = 6.dp)) {
                    Text("•  ", color = Color(0xFF6B7280))
                    Text(tip, fontSize = 13.sp, color = Color(0xFF374151))
                }
            }
        }
    }
}

@Composable
private fun NoteCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = stringResource(R.string.medical_disclaimer_icon),
                tint = Color(0xFF2962FF),
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                stringResource(R.string.medical_disclaimer),
                fontSize = 12.sp,
                color = Color(0xFF1F2937),
                lineHeight = 16.sp
            )
        }
    }
}
