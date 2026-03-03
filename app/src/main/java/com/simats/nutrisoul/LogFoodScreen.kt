package com.simats.nutrisoul

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.simats.nutrisoul.ui.FoodItemUi
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogFoodScreen(
    navController: NavController,
    viewModel: LogFoodViewModel = hiltViewModel()
) {
    val todayTotals by viewModel.todayTotals.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val localFoodDatabase = suggestedFoods
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedFood by remember { mutableStateOf<FoodItemUi?>(null) }
    var showManualSheet by remember { mutableStateOf(false) }
    var showScanResults by remember { mutableStateOf(false) }

    val targetCalories = viewModel.getTargetCaloriesOrDefault(2000.0)
    val progressRaw = if (targetCalories > 0) (todayTotals.calories / targetCalories).toFloat() else 0f
    val progress = progressRaw.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(progress, label = "")

    // --- Camera & Gallery Launchers ---
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onImageSelected(uri)
            showScanResults = true
        }
    }

    var tempUri by remember { mutableStateOf<Uri?>(null) }
    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = tempUri
        if (success && uri != null) {
            viewModel.onImageSelected(uri)
            showScanResults = true
        }
    }

    fun createTempImageUri(): Uri {
        val dir = context.externalCacheDir ?: context.cacheDir
        val file = File(dir, "scan_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
    // --------------------------------


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
    ) {
        Column(Modifier.fillMaxSize()) {

            LogFoodHeader(onBack = { navController.popBackStack() })

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-60).dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                TodayCaloriesCard(
                    calories = todayTotals.calories,
                    targetCalories = targetCalories,
                    protein = todayTotals.protein,
                    carbs = todayTotals.carbs,
                    fats = todayTotals.fats,
                    progress = animatedProgress
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    GradientActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Scan Food",
                        subtitle = "AI Detection",
                        icon = Icons.Default.CameraAlt,
                        gradient = Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF4F46E5))),
                        onClick = {
                            tempUri = createTempImageUri()
                            takePicture.launch(tempUri)
                        }
                    )

                    GradientActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Upload",
                        subtitle = "From Gallery",
                        icon = Icons.Default.Add,
                        gradient = Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                        onClick = { pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    )
                }

                Button(
                    onClick = { showManualSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1F2937)),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Manual Entry", fontWeight = FontWeight.Bold)
                }

                FoodSearchCard(
                    query = query,
                    onQueryChanged = viewModel::onQueryChanged,
                    results = searchResults,
                    onFoodPick = { selectedFood = it },
                    localFoods = localFoodDatabase,
                    onQuickAddLocal = { item ->
                        viewModel.addFood(item, item.servingQuantity)
                    }
                )
            }
        }

        selectedFood?.let { food ->
            FoodDetailsBottomSheet(
                foodItem = food,
                onDismiss = { selectedFood = null },
                onLogFood = { item, qty ->
                    viewModel.addFood(item, qty)
                    selectedFood = null
                }
            )
        }

        if (showManualSheet) {
            ManualEntryBottomSheet(
                onDismiss = { showManualSheet = false },
                onSave = { manual ->
                    viewModel.addManualFood(
                        name = manual.name,
                        quantity = manual.quantity,
                        unit = manual.unit,
                        calories = manual.calories,
                        protein = manual.protein,
                        carbs = manual.carbs,
                        fats = manual.fats
                    )
                    showManualSheet = false
                }
            )
        }

        // --- Scan Results Bottom Sheet ---
        if (showScanResults) {
            ScanResultBottomSheet(
                uiState = uiState,
                onDismiss = { showScanResults = false },
                onLogFood = { item, qty ->
                    viewModel.addFood(item, qty)
                },
                onLogAll = { items ->
                    items.forEach { item ->
                        viewModel.addFood(item, item.servingQuantity)
                    }
                },
                onManualEntry = {
                    showScanResults = false
                    showManualSheet = true
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultBottomSheet(
    uiState: LogFoodUiState,
    onDismiss: () -> Unit,
    onLogFood: (FoodItemUi, Double) -> Unit,
    onLogAll: (List<FoodItemUi>) -> Unit,
    onManualEntry: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Scan Results", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            uiState.imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Scanned image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(Modifier.height(12.dp))
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
                Text("Analyzing image...", modifier = Modifier.padding(top = 16.dp))
            } else {
                uiState.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                if (uiState.nutrition.isNotEmpty()) {
                    Text("We found these items:", fontWeight = FontWeight.Bold)
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(uiState.nutrition) { item ->
                            SearchResultRow(item = item, onClick = { onLogFood(item, item.servingQuantity) })
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { onLogAll(uiState.nutrition); onDismiss() }) {
                        Text("Log All Found Items")
                    }
                } else {
                    if (uiState.extractedText.isNotBlank()) {
                        Text("Extracted text:")
                        Text(uiState.extractedText)
                        Spacer(Modifier.height(12.dp))
                    }
                    Text("No food items recognized automatically.")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onManualEntry) {
                        Text("Enter Manually")
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}


@Composable
private fun LogFoodHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                brush = Brush.horizontalGradient(listOf(Color(0xFFFF7A18), Color(0xFFFF3D3D))),
                shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
            )
            .padding(top = 18.dp, start = 10.dp, end = 16.dp)
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
                Spacer(Modifier.width(8.dp))
                Text("🍴", fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Log Food",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Track your meals with AI",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 54.dp)
            )
        }
    }
}

@Composable
private fun TodayCaloriesCard(
    calories: Double,
    targetCalories: Double,
    protein: Double,
    carbs: Double,
    fats: Double,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Today's Calories",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${calories.roundToInt()}/${targetCalories.roundToInt()}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5722)
                )
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFE5E7EB))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFF7A18), Color(0xFFFF3D3D))
                            )
                        )
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                MacroMini("Protein", "${protein.roundToInt()}g")
                MacroMini("Carbs", "${carbs.roundToInt()}g")
                MacroMini("Fats", "${fats.roundToInt()}g")
            }
        }
    }
}

@Composable
private fun MacroMini(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 14.sp, color = Color(0xFF6B7280))
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
    }
}

@Composable
private fun GradientActionCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(42.dp))
                Spacer(Modifier.height(10.dp))
                Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun FoodSearchCard(
    query: String,
    onQueryChanged: (String) -> Unit,
    results: List<FoodItemUi>,
    onFoodPick: (FoodItemUi) -> Unit,
    localFoods: List<FoodItemUi>,
    onQuickAddLocal: (FoodItemUi) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Search Food Database",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF1F2937)
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search for food...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            if (query.length >= 2) {
                if (results.isEmpty()) {
                    Text("No foods found", color = Color(0xFF6B7280), modifier = Modifier.padding(8.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(results) { item ->
                            SearchResultRow(item, onClick = { onFoodPick(item) })
                        }
                    }
                }
            } else {
                Text("Suggested Foods", fontWeight = FontWeight.Bold, color = Color(0xFF4B5563))
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(localFoods) { item ->
                        SearchResultRow(item, isLocal = true, onClick = { onFoodPick(item) }, onQuickAdd = { onQuickAddLocal(item) })
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultRow(
    item: FoodItemUi,
    isLocal: Boolean = false,
    onClick: () -> Unit,
    onQuickAdd: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.Medium)
            Text("${item.calories.roundToInt()} kcal • ${item.servingQuantity}${item.servingUnit}", fontSize = 12.sp, color = Color.Gray)
        }
        if (isLocal && onQuickAdd != null) {
            IconButton(onClick = onQuickAdd) {
                Icon(Icons.Default.Add, contentDescription = "Quick Add", tint = Color(0xFF10B981))
            }
        }
    }
}

val suggestedFoods: List<FoodItemUi> = listOf(
    FoodItemUi(0, "Idli", 39.0, 1.4, 7.6, 0.2, 1.0, "piece"),
    FoodItemUi(0, "Dosa", 168.0, 4.0, 29.0, 3.7, 1.0, "piece"),
    FoodItemUi(0, "Chapati", 120.0, 3.5, 18.0, 3.0, 1.0, "piece"),
    FoodItemUi(0, "Rice (cooked)", 130.0, 2.7, 28.0, 0.3, 100.0, "g"),
    FoodItemUi(0, "Dal", 116.0, 9.0, 20.0, 0.4, 100.0, "g"),
    FoodItemUi(0, "Egg (boiled)", 78.0, 6.0, 0.6, 5.3, 1.0, "egg"),
    FoodItemUi(0, "Milk", 60.0, 3.2, 4.8, 3.3, 100.0, "ml"),
    FoodItemUi(0, "Banana", 89.0, 1.1, 23.0, 0.3, 100.0, "g"),
    FoodItemUi(0, "Apple", 52.0, 0.3, 14.0, 0.2, 100.0, "g"),
    FoodItemUi(0, "Chicken breast", 165.0, 31.0, 0.0, 3.6, 100.0, "g"),
    FoodItemUi(0, "Paneer", 265.0, 18.0, 6.0, 20.0, 100.0, "g")
)
