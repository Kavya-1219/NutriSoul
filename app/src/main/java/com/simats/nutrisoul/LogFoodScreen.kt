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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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

    var targetCalories by remember { mutableStateOf(2000.0) }
    LaunchedEffect(Unit) {
        targetCalories = viewModel.getTargetCaloriesOrDefault(2000.0)
    }
    
    val progressRaw = if (targetCalories > 0.0) (todayTotals.calories / targetCalories).toFloat() else 0f
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
        // ✅ Fix 2: ONE Scroll System - Professional LazyColumn structure
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                LogFoodHeader(onBack = { navController.popBackStack() })
            }

            item {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    TodayCaloriesCard(
                        calories = todayTotals.calories,
                        targetCalories = targetCalories,
                        protein = todayTotals.protein,
                        carbs = todayTotals.carbs,
                        fats = todayTotals.fats,
                        progress = animatedProgress
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
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
                        },
                        height = 120.dp
                    )

                    GradientActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Upload",
                        subtitle = "From Gallery",
                        icon = Icons.Default.Add,
                        gradient = Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                        onClick = { pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        height = 120.dp
                    )
                }
            }

            item {
                // ✅ Fix 1: Manual Entry subtitle visible (taller card + stronger text)
                Box(Modifier.padding(horizontal = 16.dp)) {
                    GradientActionCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "Manual Entry",
                        subtitle = "Type and log food",
                        icon = Icons.Default.Edit,
                        gradient = Brush.horizontalGradient(
                            listOf(Color(0xFFFF5FA2), Color(0xFFFF2D55)) // 🩷 pink gradient
                        ),
                        onClick = { showManualSheet = true },
                        height = 125.dp
                    )
                }
            }

            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    FoodSearchCard(
                        query = query,
                        onQueryChanged = viewModel::onQueryChanged,
                        results = searchResults,
                        localFoods = suggestedFoods,
                        onFoodPick = { selectedFood = it },
                        onQuickAddLocal = { item ->
                            viewModel.addFood(item, item.servingQuantity)
                        }
                    )
                }
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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.nutrition.forEach { item ->
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
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit,
    height: Dp = 120.dp
) {
    Card(
        modifier = modifier
            .height(height)
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
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodSearchCard(
    query: String,
    onQueryChanged: (String) -> Unit,
    results: List<FoodItemUi>,
    localFoods: List<FoodItemUi>,
    onFoodPick: (FoodItemUi) -> Unit,
    onQuickAddLocal: (FoodItemUi) -> Unit
) {
    var showAllSuggested by remember { mutableStateOf(false) }

    val localMatches = remember(query) {
        localFoods.filter { it.name.contains(query, ignoreCase = true) }
    }
    val showOnline = results.isNotEmpty()
    val showLocalFallback = results.isEmpty() && localMatches.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Search Food",
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
                when {
                    showOnline -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            results.forEach { item ->
                                SearchResultRow(item, onClick = { onFoodPick(item) })
                            }
                        }
                    }

                    showLocalFallback -> {
                        Text("Showing local results", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            localMatches.forEach { item ->
                                SearchResultRow(
                                    item = item,
                                    isLocal = true,
                                    onClick = { onFoodPick(item) },
                                    onQuickAdd = { onQuickAddLocal(item) }
                                )
                            }
                        }
                    }

                    else -> {
                        Text("No foods found", color = Color(0xFF6B7280), modifier = Modifier.padding(8.dp))
                    }
                }
            } else {
                Text("Suggested Foods", fontWeight = FontWeight.Bold, color = Color(0xFF4B5563))
                Spacer(Modifier.height(8.dp))
                
                // ✅ Fix 3: Show 5 items + "See all" (no nested scroll conflict)
                val suggestedToShow = if (showAllSuggested) localFoods else localFoods.take(5)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestedToShow.forEach { item ->
                        SearchResultRow(
                            item = item,
                            isLocal = true,
                            onClick = { onFoodPick(item) },
                            onQuickAdd = { onQuickAddLocal(item) }
                        )
                    }

                    if (!showAllSuggested && localFoods.size > 5) {
                        TextButton(
                            onClick = { showAllSuggested = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("See all suggested foods", color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold)
                        }
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
