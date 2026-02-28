package com.simats.nutrisoul

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun StressAndSleepScreen(
    navController: NavController,
    viewModel: StressAndSleepViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val alarmPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.onReminderToggled(true, context)
            }
        }
    )

    StressAndSleepScreenContent(navController, uiState, viewModel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                viewModel.onReminderToggled(it, context)
            } else {
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also {
                    context.startActivity(it)
                }
            }
        } else {
            viewModel.onReminderToggled(it, context)
        }
    }

    if (uiState.showSleepScheduleDialog) {
        EditSleepScheduleDialog(
            schedule = uiState.sleepSchedule,
            onDismiss = viewModel::onDismissScheduleDialog,
            onSave = { bedtime, wakeTime ->
                viewModel.onSaveSchedule(bedtime, wakeTime, context)
            }
        )
    }

    if (uiState.showWindDownDialog) {
        WindDownDialog(
            onDismiss = viewModel::onDismissWindDownDialog,
            onStart = {
                viewModel.onDismissWindDownDialog()
                viewModel.onStartBreathing()
            },
            onSnooze = {
                viewModel.onDismissWindDownDialog()
                // Snooze logic
            }
        )
    }

    if (uiState.showLogSleepDialog) {
        LogSleepDialog(
            schedule = uiState.sleepSchedule,
            onDismiss = viewModel::onDismissLogSleepDialog,
            onLog = viewModel::onLogSleep
        )
    }
}

@Composable
fun StressAndSleepScreenContent(
    navController: NavController,
    uiState: StressAndSleepUiState,
    viewModel: StressAndSleepViewModel,
    onReminderToggled: (Boolean) -> Unit
) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF0F2F5))
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF1DE9B6), Color(0xFF00BFA5))
                        )
                    )
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 48.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Stress & Sleep Support",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Nutrition-focused guidance for calm mind, better sleep, and healthy eating",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-32).dp)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                WhyStressMattersCard()
                SleepTrackingSection(
                    uiState = uiState,
                    onEditClick = viewModel::onEditScheduleClicked,
                    onLogClick = viewModel::onLogTodaySleepClicked,
                    onReminderToggled = onReminderToggled,
                    reminderEnabled = uiState.reminderEnabled
                )
                RecentSleepHistoryCard(sleepLogs = uiState.sleepLogs)
                FoodsToReduceStressSection()
                NutritionTipsForSleepSection()
                QuickCalmToolsSection(isBreathing = uiState.isBreathing, onStart = viewModel::onStartBreathing, onStop = viewModel::onStopBreathing)
                WhyThisMattersInfoCard()
            }
        }
    }
}

@Composable
fun SleepTrackingSection(
    uiState: StressAndSleepUiState,
    onEditClick: () -> Unit,
    onLogClick: () -> Unit,
    onReminderToggled: (Boolean) -> Unit,
    reminderEnabled: Boolean
) {
    val todaySleepLog = uiState.sleepLogs.firstOrNull { it.date.isEqual(java.time.LocalDate.now()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(icon = Icons.Default.NightsStay, title = "Sleep Tracking")
        Text("Quality sleep for a healthier you", color = Color.Gray, fontSize = 14.sp)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.NightsStay,
                    contentDescription = "Moon",
                    tint = Color(0xFF7E57C2),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Last Night's Sleep", color = Color.Gray)
                if (todaySleepLog != null) {
                    Text(todaySleepLog.duration, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("--", fontSize = 48.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (todaySleepLog != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = todaySleepLog.quality.bgColor)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${todaySleepLog.quality.emoji} ${todaySleepLog.quality.label}", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = todaySleepLog.quality.color)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SleepInfoChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Alarm,
                        value = uiState.sleepSchedule.bedtime.format(DateTimeFormatter.ofPattern("h:mm a")),
                        label = "Bedtime",
                        containerColor = Color(0xFFF3E5F5)
                    )
                    SleepInfoChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.TrendingUp,
                        value = "${String.format("%.1f", uiState.weeklyAverageHours)}h",
                        label = "7-Day Avg",
                        containerColor = Color(0xFFF3E5F5)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
                ) {
                    Text("Edit Sleep Schedule", modifier = Modifier.padding(vertical = 8.dp), color = Color.White)
                }
                OutlinedButton(
                    onClick = onLogClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF1DE9B6))
                ) {
                     Text("Log Today's Sleep", color = Color(0xFF1DE9B6), modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
        BedtimeReminderCard(reminderEnabled = reminderEnabled, onReminderToggled = onReminderToggled)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSleepDialog(
    schedule: SleepSchedule,
    onDismiss: () -> Unit,
    onLog: (LocalTime, LocalTime, SleepQuality) -> Unit
) {
    var bedtime by remember { mutableStateOf(schedule.bedtime) }
    var wakeTime by remember { mutableStateOf(schedule.wakeTime) }
    var quality by remember { mutableStateOf(SleepQuality.Good) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Log Your Sleep", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(24.dp))

                TimePicker(label = "Bedtime", time = bedtime, onTimeChange = { bedtime = it })
                Spacer(modifier = Modifier.height(16.dp))
                TimePicker(label = "Wake-up Time", time = wakeTime, onTimeChange = { wakeTime = it })
                Spacer(modifier = Modifier.height(16.dp))
                SleepQualitySelector(selectedQuality = quality, onQualitySelected = { quality = it })

                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onLog(bedtime, wakeTime, quality) }, modifier = Modifier.weight(1f)) {
                        Text("Log")
                    }
                }
            }
        }
    }
}

@Composable
fun SleepQualitySelector(selectedQuality: SleepQuality, onQualitySelected: (SleepQuality) -> Unit) {
    Column {
        Text("Sleep Quality", fontWeight = FontWeight.Medium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            SleepQuality.values().forEach { quality ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onQualitySelected(quality) }
                ) {
                    Text(quality.emoji, fontSize = 32.sp)
                    Text(quality.label)
                    RadioButton(
                        selected = selectedQuality == quality,
                        onClick = { onQualitySelected(quality) }
                    )
                }
            }
        }
    }
}

@Composable
fun BedtimeReminderCard(reminderEnabled: Boolean, onReminderToggled: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Bedtime Reminder", fontWeight = FontWeight.Bold)
                Text("Get notified at bedtime", color = Color.Gray)
            }
            Switch(checked = reminderEnabled, onCheckedChange = onReminderToggled)
        }
    }
}

@Composable
fun WindDownDialog(onDismiss: () -> Unit, onStart: () -> Unit, onSnooze: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.NightsStay,
                    contentDescription = "Moon",
                    tint = Color(0xFF7E57C2),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("It's bedtime", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Sleeping on time helps digestion and reduces cravings tomorrow.", textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = "", tint = Color(0xFF7E57C2))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Quality sleep helps control hunger hormones and supports mindful eating.", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
                ) {
                    Text("Start Wind-Down", color = Color.White)
                }
                TextButton(onClick = onSnooze, modifier = Modifier.fillMaxWidth()) {
                    Text("Remind Me in 10 Minutes")
                }
            }
        }
    }
}

@Composable
fun RecentSleepHistoryCard(sleepLogs: List<SleepLog>) {
    if (sleepLogs.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(icon = Icons.Default.History, title = "Recent Sleep History")
                Spacer(modifier = Modifier.height(16.dp))
                sleepLogs.forEach { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = log.quality.bgColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(log.date.format(DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)), fontWeight = FontWeight.Bold)
                                Text("🌙 ${log.bedtime.format(DateTimeFormatter.ofPattern("h:mm a"))} - ☀️ ${log.wakeTime.format(DateTimeFormatter.ofPattern("h:mm a"))}", color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${log.quality.emoji} ${log.quality.label}", fontWeight = FontWeight.Medium, color = log.quality.color)
                                Text(log.duration, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickCalmToolsSection(isBreathing: Boolean, onStart: () -> Unit, onStop: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(icon = Icons.Default.SelfImprovement, title = "Quick Calm Tools")
        Text("Simple breathing technique to manage stress and support mindful eating", color = Color.Gray, fontSize = 14.sp)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = "Breathing",
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier
                            .background(Color.White, CircleShape)
                            .padding(10.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("5-Minute Calm Breathing", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Simple breathing pattern to calm mind before meals", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (isBreathing) {
                    BreathingCircle(onStop = onStop)
                } else {
                    Button(
                        onClick = onStart,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                    ) {
                        Text("Start Breathing Exercise", modifier = Modifier.padding(vertical = 8.dp), color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BreathingCircle(onStop: () -> Unit) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.3f,
            animationSpec = repeatable(
                iterations = AnimationConstants.DefaultDurationMillis,
                animation = tween(durationMillis = 4000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(128.dp)
                .scale(scale.value)
                .background(
                    brush = Brush.radialGradient(colors = listOf(Color(0xFF81D4FA), Color(0xFF039BE5))),
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(if (scale.value > 1.15f) "Exhale" else "Inhale", fontWeight = FontWeight.Medium, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onStop,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043))
        ) {
            Text("Stop", color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSleepScheduleDialog(schedule: SleepSchedule, onDismiss: () -> Unit, onSave: (LocalTime, LocalTime) -> Unit) {
    var bedtime by remember { mutableStateOf(schedule.bedtime) }
    var wakeTime by remember { mutableStateOf(schedule.wakeTime) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Edit Sleep Schedule", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(24.dp))

                TimePicker(label = "Bedtime", time = bedtime, onTimeChange = { bedtime = it })
                Spacer(modifier = Modifier.height(16.dp))
                TimePicker(label = "Wake-up Time", time = wakeTime, onTimeChange = { wakeTime = it })

                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onSave(bedtime, wakeTime) }, modifier = Modifier.weight(1f)) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun TimePicker(label: String, time: LocalTime, onTimeChange: (LocalTime) -> Unit) {
    var hourString by remember { mutableStateOf(time.hour.toString()) }
    var minuteString by remember { mutableStateOf(time.minute.toString().padStart(2, '0')) }

    LaunchedEffect(time) {
        hourString = time.hour.toString()
        minuteString = time.minute.toString().padStart(2, '0')
    }

    Column {
        Text(label, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = hourString,
                onValueChange = { newHour ->
                    if (newHour.length <= 2) {
                        hourString = newHour.filter { it.isDigit() }
                        val hour = hourString.toIntOrNull()
                        if (hour != null && hour in 0..23) {
                            onTimeChange(time.withHour(hour))
                        } else if (hourString.isEmpty()) {
                            onTimeChange(time.withHour(0))
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                label = { Text("Hour") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = minuteString,
                onValueChange = { newMinute ->
                    if (newMinute.length <= 2) {
                        minuteString = newMinute.filter { it.isDigit() }
                        val minute = minuteString.toIntOrNull()
                        if (minute != null && minute in 0..59) {
                            onTimeChange(time.withMinute(minute))
                        } else if (minuteString.isEmpty()) {
                            onTimeChange(time.withMinute(0))
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                label = { Text("Minute") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

@Composable
fun WhyStressMattersCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1DE9B6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Heart Icon",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Why Stress & Sleep Matter for Nutrition", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                InfoPoint(text = "Stress affects digestion and appetite regulation")
                InfoPoint(text = "Poor sleep increases cravings and tendency to overeat")
                InfoPoint(text = "Hormonal balance (PCOS, diabetes) is influenced by stress and sleep quality")
            }
        }
    }
}

@Composable
fun InfoPoint(text: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 4.dp)) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp, end = 8.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF1DE9B6))
        )
        Text(text, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
    }
}

@Composable
fun SleepInfoChip(modifier: Modifier = Modifier, icon: ImageVector, value: String, label: String, containerColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color(0xFF7E57C2))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(label, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun FoodsToReduceStressSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(icon = Icons.Default.Spa, title = "Foods That Help Reduce Stress")
        FoodStressCard(
            icon = Icons.Default.Eco,
            title = "Magnesium-Rich Foods",
            description = "Almonds, spinach, pumpkin seeds, dark chocolate",
            subtext = "Helps calm nervous system naturally",
            containerColor = Color(0xFFE8F5E9),
            iconColor = Color(0xFF388E3C)
        )
        FoodStressCard(
            icon = Icons.Default.BreakfastDining,
            title = "Complex Carbohydrates",
            description = "Millets, oats, brown rice, quinoa",
            subtext = "Stabilizes mood and energy levels",
            containerColor = Color(0xFFFFF3E0),
            iconColor = Color(0xFFF57C00)
        )
        FoodStressCard(
            icon = Icons.Default.EmojiFoodBeverage,
            title = "Herbal Teas & Hydration",
            description = "Chamomile tea, tulsi tea, warm water with lemon",
            subtext = "Promotes relaxation and reduces tension",
            containerColor = Color(0xFFE0F7FA),
            iconColor = Color(0xFF0097A7)
        )
    }
}

@Composable
fun FoodStressCard(icon: ImageVector, title: String, description: String, subtext: String, containerColor: Color, iconColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier
                    .background(Color.White, CircleShape)
                    .padding(10.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(description, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
                Text(subtext, fontSize = 12.sp, color = Color.Gray, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun NutritionTipsForSleepSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(icon = Icons.Default.NightsStay, title = "Nutrition Tips for Better Sleep")
         NutritionTipCard(
            icon = Icons.Default.DinnerDining,
            title = "Light Dinner Timing",
            description = "Eat dinner 2-3 hours before bedtime for better digestion",
            containerColor = Color(0xFFEDE7F6)
        )
         NutritionTipCard(
            icon = Icons.Default.Fastfood,
            title = "Sleep-Supporting Foods",
            description = "Include foods rich in tryptophan like milk, bananas, dates",
            containerColor = Color(0xFFF3E5F5)
        )
         NutritionTipCard(
            icon = Icons.Default.NoFood,
            title = "Avoid Late Stimulants",
            description = "Skip caffeine after 2 PM and avoid heavy meals at night",
            containerColor = Color(0xFFFFEBEE)
        )
    }
}

@Composable
fun NutritionTipCard(icon: ImageVector, title: String, description: String, containerColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF5E35B1),
                modifier = Modifier
                    .background(Color.White, CircleShape)
                    .padding(10.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(description, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
            }
        }
    }
}


@Composable
fun WhyThisMattersInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        border = BorderStroke(1.dp, Color(0xFFB9F6CA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Why this matters:", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Sleep timing and quality affect hunger hormones, digestion, and food choices. Managing stress and sleep through nutrition and and simple calm tools supports your overall health goals.",
                fontSize = 14.sp,
                color = Color(0xFF2E7D32),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = title, tint = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun MindCareScreenPreview() {
    StressAndSleepScreen(rememberNavController())
}

fun scheduleBedtimeReminder(context: Context, bedtime: LocalTime) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, BedtimeReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
        // Handle case where permission is not granted
        return
    }

    val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, bedtime.hour)
        set(Calendar.MINUTE, bedtime.minute)
        set(Calendar.SECOND, 0)
        if (before(Calendar.getInstance())) {
            add(Calendar.DATE, 1)
        }
    }

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
}
