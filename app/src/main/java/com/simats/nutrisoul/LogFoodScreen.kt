package com.simats.nutrisoul

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
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
import androidx.core.content.ContextCompat
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedFood by remember { mutableStateOf<FoodItemUi?>(null) }
    var showManualSheet by remember { mutableStateOf(false) }
    var showScanResults by remember { mutableStateOf(false) }

    val targetCalories = viewModel.getTargetCaloriesOrDefault(2000.0)
    val progressRaw = if (targetCalories > 0) (todayTotals.calories / targetCalories).toFloat() else 0f
    val progress = progressRaw.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(progress, label = "")

    // --- Search Logic with Local Fallback ---
    val localMatches = remember(query) {
        if (query.length < 2) emptyList()
        else suggestedFoods.filter { it.name.contains(query, ignoreCase = true) }
    }
    // ----------------------------------------

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

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tempUri = createTempImageUri(context)
            takePicture.launch(tempUri)
        } else {
            Toast.makeText(context, "Camera permission is required to scan food.", Toast.LENGTH_SHORT).show()
        }
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
                    .offset(y = (-35).dp), // Fixed offset for better spacing
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
                            when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
                                PackageManager.PERMISSION_GRANTED -> {
                                    tempUri = createTempImageUri(context)
                                    takePicture.launch(tempUri)
                                }
                                else -> {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
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
                    modifier = Modifier.fillMaxWidth().height(56.dp),
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
                    searchResults = searchResults,
                    localMatches = localMatches,
                    onFoodPick = { selectedFood = it },
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

private fun createTempImageUri(context: android.content.Context): Uri {
    val dir = context.externalCacheDir ?: context.cacheDir
    val file = File(dir, "scan_${System.currentTimeMillis()}.jpg").apply { 
        createNewFile()
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
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
                        items(uiState.nutrition, key = { it.id }) { item ->
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
            .height(160.dp)
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
    searchResults: List<FoodItemUi>,
    localMatches: List<FoodItemUi>,
    onFoodPick: (FoodItemUi) -> Unit,
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
                modifier = Modifier.fillMaxWidth().height(56.dp),
                placeholder = { Text("Search for food...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            if (query.length >= 2) {
                if (searchResults.isEmpty()) {
                    if (localMatches.isEmpty()) {
                        Text("No online match found", color = Color(0xFF6B7280), fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                    } else {
                        Text("No online match, showing local results", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                        Spacer(Modifier.height(4.dp))
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(localMatches, key = { it.id }) { item ->
                                SearchResultRow(item, isLocal = true, onClick = { onFoodPick(item) }, onQuickAdd = { onQuickAddLocal(item) })
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults, key = { it.id }) { item ->
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
                    items(suggestedFoods, key = { it.id }) { item ->
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
    FoodItemUi(1, "Idli", 39.0, 1.4, 7.6, 0.2, 1.0, "piece"),
    FoodItemUi(2, "Dosa", 168.0, 4.0, 29.0, 3.7, 1.0, "piece"),
    FoodItemUi(3, "Chapati", 120.0, 3.5, 18.0, 3.0, 1.0, "piece"),
    FoodItemUi(4, "Samosa", 262.0, 5.0, 32.0, 13.0, 1.0, "piece"),
    FoodItemUi(5, "Vada", 170.0, 4.0, 20.0, 8.0, 1.0, "piece"),
    FoodItemUi(6, "Pani Puri", 45.0, 1.0, 7.0, 1.5, 1.0, "piece"),
    FoodItemUi(7, "Poha", 180.0, 4.0, 32.0, 4.0, 1.0, "bowl"),
    FoodItemUi(8, "Upma", 210.0, 6.0, 34.0, 6.0, 1.0, "bowl"),
    FoodItemUi(9, "Bonda", 200.0, 4.0, 25.0, 9.0, 1.0, "piece"),
    FoodItemUi(10, "Biscuits", 70.0, 1.0, 10.0, 3.0, 2.0, "pieces"),
    FoodItemUi(11, "Tea", 30.0, 1.0, 5.0, 1.0, 200.0, "ml"),
    FoodItemUi(12, "Coffee", 50.0, 2.0, 6.0, 2.0, 200.0, "ml"),
    FoodItemUi(13, "Maggie", 310.0, 8.0, 45.0, 12.0, 1.0, "pack"),
    FoodItemUi(14, "Potato Chips", 536.0, 7.0, 53.0, 35.0, 100.0, "g"),
    FoodItemUi(15, "Chocolate", 546.0, 5.0, 61.0, 31.0, 100.0, "g"),
    FoodItemUi(16, "Veg Puff", 250.0, 4.0, 30.0, 15.0, 1.0, "piece"),
    FoodItemUi(17, "Bread Jam", 150.0, 3.0, 30.0, 2.0, 1.0, "slice"),
    FoodItemUi(18, "Omelette", 154.0, 11.0, 1.0, 12.0, 1.0, "piece"),
    FoodItemUi(19, "Sandwich", 250.0, 8.0, 35.0, 10.0, 1.0, "piece"),
    FoodItemUi(20, "Fruit Salad", 50.0, 1.0, 12.0, 0.0, 100.0, "g"),
    FoodItemUi(21, "Burger", 295.0, 13.0, 30.0, 14.0, 1.0, "piece"),
    FoodItemUi(22, "Pizza Slice", 285.0, 12.0, 36.0, 10.0, 1.0, "slice"),
    FoodItemUi(23, "Gulab Jamun", 150.0, 2.0, 25.0, 7.0, 1.0, "piece"),
    FoodItemUi(24, "Lassi", 150.0, 3.5, 20.0, 4.0, 250.0, "ml"),
    FoodItemUi(25, "Biryani", 350.0, 15.0, 45.0, 12.0, 1.0, "plate")
)
