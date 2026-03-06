package com.simats.nutrisoul

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.simats.nutrisoul.data.meal.model.Meal
import com.simats.nutrisoul.data.meal.model.MealPlan
import com.simats.nutrisoul.data.meal.model.totalCalories
import com.simats.nutrisoul.data.meal.model.totalCarbs
import com.simats.nutrisoul.data.meal.model.totalFats
import com.simats.nutrisoul.data.meal.model.totalProtein
import com.simats.nutrisoul.ui.theme.LocalDarkTheme
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodaysMealPlanScreen(
    navController: NavController,
    viewModel: MealPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var sheetMealType by remember { mutableStateOf<String?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(navController = navController) },
        topBar = {
            TopAppBar(
                title = { Text("Today's Meal Plan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            when (val s = uiState) {
                is MealPlanUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is MealPlanUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(s.message, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                is MealPlanUiState.Ready -> {
                    val plan = s.plan
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            Spacer(Modifier.height(12.dp))
                            PremiumHeader(plan)
                            Spacer(Modifier.height(12.dp))
                            PersonalizedBanner(
                                goal = viewModel.currentProfile().goal,
                                diet = viewModel.currentProfile().dietType
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        items(plan.meals) { meal ->
                            MealCard(
                                meal = meal,
                                onEdit = {
                                    sheetMealType = meal.mealType
                                    showSheet = true
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        item {
                            InfoNote()
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showSheet && sheetMealType != null) {
        val mealType = sheetMealType!!
        val alternatives = viewModel.getAlternatives(mealType)

        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "Change ${mealType.replaceFirstChar { it.uppercase() }}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Pick an alternative meal (instant swap).",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                alternatives.forEach { alt ->
                    AlternativeMealRow(
                        meal = alt,
                        onSelect = {
                            viewModel.swapMeal(mealType, alt)
                            showSheet = false
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PremiumHeader(plan: MealPlan) {
    val total = plan.totalCalories()
    val target = plan.targetCalories
    val progress = if (target <= 0) 0f else min(1f, total.toFloat() / target.toFloat())
    val delta = target - total

    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF2ECC71), Color(0xFF1FA855))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = gradient, shape = RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text("Today's Meal Plan", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Total: $total kcal", color = Color.White.copy(alpha = 0.95f))
        Text(
            "Protein: ${plan.totalProtein()}g • Carbs: ${plan.totalCarbs()}g • Fats: ${plan.totalFats()}g",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 13.sp
        )

        Spacer(Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.25f)
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Target: $target kcal", color = Color.White.copy(alpha = 0.95f), fontSize = 14.sp)

            val badgeColor = if (delta >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            val badgeText = if (delta >= 0) "${delta} kcal left" else "${-delta} kcal over"

            Box(
                modifier = Modifier
                    .background(badgeColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    badgeText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF1B5E20)
                )
            }
        }
    }
}

@Composable
private fun PersonalizedBanner(goal: String, diet: String) {
    val isDark = LocalDarkTheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if(isDark) Color(0xFF1E3A8A).copy(alpha=0.3f) else Color(0xFFEAF2FF), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = if(isDark) Color(0xFF93C5FD) else Color(0xFF2F5BEA))
        Spacer(Modifier.width(10.dp))
        Column {
            Text("Personalized for Your Goals", fontWeight = FontWeight.Bold, color = if(isDark) Color(0xFF93C5FD) else Color(0xFF1A3DBA))
            Text(
                "This plan is customized for ${goal.replace('_', ' ')} and your $diet diet.",
                fontSize = 12.sp,
                color = if(isDark) Color(0xFFA5B4FC) else Color(0xFF1A3DBA)
            )
        }
    }
}

@Composable
private fun MealCard(meal: Meal, onEdit: () -> Unit) {
    val isDark = LocalDarkTheme.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(mealIcon(meal.mealType), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text(meal.mealType.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Text(meal.title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MacroChip("Calories", "${meal.calories}", if(isDark) Color(0xFF064E3B).copy(alpha=0.4f) else Color(0xFFE8F5E9), if(isDark) Color(0xFFA7F3D0) else Color(0xFF2E7D32))
                MacroChip("Protein", "${meal.protein}g", if(isDark) Color(0xFF1E3A8A).copy(alpha=0.4f) else Color(0xFFE3F2FD), if(isDark) Color(0xFF93C5FD) else Color(0xFF1565C0))
                MacroChip("Carbs", "${meal.carbs}g", if(isDark) Color(0xFF7C2D12).copy(alpha=0.4f) else Color(0xFFFFF3E0), if(isDark) Color(0xFFFDBA74) else Color(0xFFEF6C00))
                MacroChip("Fats", "${meal.fats}g", if(isDark) Color(0xFF7F1D1D).copy(alpha=0.4f) else Color(0xFFFFEBEE), if(isDark) Color(0xFFFECACA) else Color(0xFFC62828))
            }

            Spacer(Modifier.height(12.dp))
            Text("ITEMS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(8.dp))
            meal.items.forEach { item ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text(item.quantity, fontSize = 12.sp, color = Color(0xFF2ECC71))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${item.calories} kcal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "P:${item.protein}g C:${item.carbs}g F:${item.fats}g",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun MacroChip(label: String, value: String, bg: Color, fg: Color) {
    Column(
        modifier = Modifier
            .background(bg, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 11.sp, color = fg)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

@Composable
private fun AlternativeMealRow(meal: Meal, onSelect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(mealIcon(meal.mealType), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(meal.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "${meal.calories} kcal • P ${meal.protein}g • C ${meal.carbs}g • F ${meal.fats}g",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("Select", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InfoNote() {
    val isDark = LocalDarkTheme.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if(isDark) Color(0xFF4A148C).copy(alpha=0.2f) else Color(0xFFF3E5F5))
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Info, contentDescription = null, tint = if(isDark) Color(0xFFCE93D8) else Color(0xFF6A1B9A))
            Spacer(Modifier.width(10.dp))
            Text(
                "Note: This meal plan is generated based on your profile. Tap edit to swap meals or refresh to generate a new plan.",
                color = if(isDark) Color(0xFFE1BEE7) else Color(0xFF4A148C),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun mealIcon(type: String) = when (type.lowercase()) {
    "breakfast" -> Icons.Default.BreakfastDining
    "lunch" -> Icons.Default.LunchDining
    "dinner" -> Icons.Default.DinnerDining
    "snack" -> Icons.Default.Fastfood
    else -> Icons.Default.Restaurant
}
