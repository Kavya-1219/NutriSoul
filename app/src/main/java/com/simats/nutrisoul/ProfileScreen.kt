package com.simats.nutrisoul

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.simats.nutrisoul.data.User
import com.simats.nutrisoul.data.UserViewModel
import com.simats.nutrisoul.ui.theme.LocalDarkTheme

@Composable
fun ProfileScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val user by userViewModel.user.collectAsStateWithLifecycle()
    val profilePictureUri by userViewModel.profilePictureUri.collectAsStateWithLifecycle()

    var isEditing by rememberSaveable { mutableStateOf(false) }
    var showPasswordChange by rememberSaveable { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(it, flags)
                } catch (_: SecurityException) {}
                userViewModel.setProfilePictureUri(it)
            }
        }
    )

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Success") },
            text = { Text(successMessage) },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) { Text("OK") }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->
        val currentUser = user
        if (currentUser == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Loading profile...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            ProfileHeaderPremium(
                navController = navController,
                isEditing = isEditing,
                onToggleEdit = {
                    isEditing = !isEditing
                    if (isEditing) showPasswordChange = false
                },
                userName = currentUser.name.ifBlank { "User" },
                profilePictureUri = profilePictureUri,
                onImageClick = { imagePickerLauncher.launch(arrayOf("image/*")) }
            )

            ProfilePremiumContent(
                user = currentUser,
                isEditing = isEditing,
                showPasswordChange = showPasswordChange,
                onTogglePasswordChange = { showPasswordChange = !showPasswordChange },
                onCancelChanges = { isEditing = false },
                onSaveUser = { updated ->
                    userViewModel.updateUser(updated)
                    isEditing = false
                    showPasswordChange = false
                    successMessage = "Profile updated successfully!"
                    showSuccessDialog = true
                },
                onPasswordChanged = { updated ->
                    userViewModel.updateUser(updated)
                    showPasswordChange = false
                    successMessage = "Password updated successfully!"
                    showSuccessDialog = true
                }
            )
        }
    }
}

@Composable
private fun ProfileHeaderPremium(
    navController: NavController,
    isEditing: Boolean,
    onToggleEdit: () -> Unit,
    userName: String,
    profilePictureUri: Uri?,
    onImageClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF22C55E), Color(0xFF16A34A))
                ),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Text(
                text = "Profile",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            if (!isEditing) {
                IconButton(
                    onClick = onToggleEdit,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                }
            } else {
                Spacer(Modifier.size(48.dp))
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { onImageClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePictureUri != null) {
                        AsyncImage(
                            model = profilePictureUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(32.dp)
                        .background(Color(0xFF16A34A), CircleShape)
                        .clip(CircleShape)
                        .clickable { onImageClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change Picture",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = userName,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProfilePremiumContent(
    user: User,
    isEditing: Boolean,
    showPasswordChange: Boolean,
    onTogglePasswordChange: () -> Unit,
    onCancelChanges: () -> Unit,
    onSaveUser: (User) -> Unit,
    onPasswordChanged: (User) -> Unit
) {
    var name by rememberSaveable(user.email) { mutableStateOf(user.name) }
    var age by rememberSaveable(user.email) { mutableStateOf(if (user.age == 0) "" else user.age.toString()) }
    var gender by rememberSaveable(user.email) { mutableStateOf(user.gender) }
    var height by rememberSaveable(user.email) { mutableStateOf(if (user.height == 0f) "" else user.height.toString()) }
    var weight by rememberSaveable(user.email) { mutableStateOf(if (user.weight == 0f) "" else user.weight.toString()) }
    var activityLevel by rememberSaveable(user.email) { mutableStateOf(user.activityLevel) }
    var goal by rememberSaveable(user.email) { mutableStateOf(user.goal) }
    var targetWeight by rememberSaveable(user.email) { mutableStateOf(if (user.targetWeight == 0f) "" else user.targetWeight.toString()) }
    var mealsPerDay by rememberSaveable(user.email) { mutableStateOf(if (user.mealsPerDay == 0) "" else user.mealsPerDay.toString()) }
    var healthConditions by rememberSaveable(user.email) { mutableStateOf(user.healthConditions.joinToString(", ")) }

    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user) {
        if (!isEditing) {
            name = user.name
            age = if (user.age == 0) "" else user.age.toString()
            gender = user.gender
            height = if (user.height == 0f) "" else user.height.toString()
            weight = if (user.weight == 0f) "" else user.weight.toString()
            activityLevel = user.activityLevel
            goal = user.goal
            targetWeight = if (user.targetWeight == 0f) "" else user.targetWeight.toString()
            mealsPerDay = if (user.mealsPerDay == 0) "" else user.mealsPerDay.toString()
            healthConditions = user.healthConditions.joinToString(", ")
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .offset(y = (-44).dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CardPremium {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Email",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = user.email.ifBlank { "—" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        CardPremium {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Personal Information")

                PremiumField(
                    label = "Name",
                    value = name,
                    enabled = isEditing,
                    onValueChange = { name = it }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    PremiumField(
                        label = "Age",
                        value = age,
                        enabled = isEditing,
                        onValueChange = { age = it.filter { ch -> ch.isDigit() }.take(3) },
                        modifier = Modifier.weight(1f)
                    )
                    PremiumDropdown(
                        label = "Gender",
                        value = gender,
                        enabled = isEditing,
                        options = listOf("Male", "Female", "Other"),
                        onSelect = { gender = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        CardPremium {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Physical Attributes")

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    PremiumField(
                        label = "Height (cm)",
                        value = height,
                        enabled = isEditing,
                        onValueChange = { height = it.filter { ch -> ch.isDigit() || ch == '.' }.take(6) },
                        modifier = Modifier.weight(1f)
                    )
                    PremiumField(
                        label = "Weight (kg)",
                        value = weight,
                        enabled = isEditing,
                        onValueChange = { weight = it.filter { ch -> ch.isDigit() || ch == '.' }.take(6) },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                PremiumDropdown(
                    label = "Activity Level",
                    value = activityLevel,
                    enabled = isEditing,
                    options = listOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Extra Active"),
                    onSelect = { activityLevel = it }
                )
            }
        }

        CardPremium {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Goals & Nutrition")

                PremiumDropdown(
                    label = "Your Goal",
                    value = goal,
                    enabled = isEditing,
                    options = listOf("Lose Weight", "Maintain Weight", "Gain Weight", "Build Muscle"),
                    onSelect = { goal = it }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    PremiumField(
                        label = "Target Weight (kg)",
                        value = targetWeight,
                        enabled = isEditing,
                        onValueChange = { targetWeight = it.filter { ch -> ch.isDigit() || ch == '.' }.take(6) },
                        modifier = Modifier.weight(1f)
                    )
                    PremiumField(
                        label = "Meals Per Day",
                        value = mealsPerDay,
                        enabled = isEditing,
                        onValueChange = { mealsPerDay = it.filter { ch -> ch.isDigit() }.take(1) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        CardPremium {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Health Profile")
                
                PremiumField(
                    label = "Health Conditions (comma separated)",
                    value = healthConditions,
                    enabled = isEditing,
                    onValueChange = { healthConditions = it }
                )

                if (isEditing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onCancelChanges()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val updated = user.copy(
                                    name = name.trim(),
                                    age = age.toIntOrNull() ?: 0,
                                    gender = gender.trim(),
                                    height = height.toFloatOrNull() ?: 0f,
                                    weight = weight.toFloatOrNull() ?: 0f,
                                    activityLevel = activityLevel,
                                    goal = goal,
                                    targetWeight = targetWeight.toFloatOrNull() ?: 0f,
                                    mealsPerDay = mealsPerDay.toIntOrNull() ?: 0,
                                    healthConditions = healthConditions.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                )
                                onSaveUser(updated)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save")
                        }
                    }
                }
            }
        }

        CardPremium {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Change Password",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    TextButton(
                        onClick = {
                            passwordError = null
                            onTogglePasswordChange()
                        }
                    ) {
                        Text(if (showPasswordChange) "Hide" else "Edit")
                    }
                }

                if (showPasswordChange) {
                    passwordError?.let {
                        ErrorInlineCard(it)
                    }

                    PasswordInput(
                        label = "Current Password",
                        value = currentPassword,
                        onValueChange = { currentPassword = it }
                    )

                    PasswordInput(
                        label = "New Password",
                        value = newPassword,
                        onValueChange = { newPassword = it }
                    )

                    PasswordInput(
                        label = "Confirm New Password",
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it }
                    )

                    Text(
                        text = "At least 8 characters + 1 special character",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            passwordError = null

                            if (user.password.isNotBlank() && currentPassword != user.password) {
                                passwordError = "Current password is incorrect"
                                return@Button
                            }

                            if (user.password.isNotBlank() && currentPassword.isBlank()) {
                                passwordError = "Please enter your current password"
                                return@Button
                            }

                            if (newPassword.length < 8) {
                                passwordError = "New password must be at least 8 characters"
                                return@Button
                            }
                            val specialCharRegex = Regex("""[!@#$%^&*(),.?":{}|<>]""")
                            if (!specialCharRegex.containsMatchIn(newPassword)) {
                                passwordError = "New password must include at least 1 special character"
                                return@Button
                            }
                            if (newPassword != confirmPassword) {
                                passwordError = "New passwords do not match"
                                return@Button
                            }

                            val updated = user.copy(password = newPassword)

                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""

                            onPasswordChanged(updated)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Text("Update Password")
                    }
                }
            }
        }
    }
}

@Composable
private fun CardPremium(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        content()
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun PremiumField(
    label: String,
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))

        if (enabled) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Text(
                    text = if (value.isBlank()) "—" else value,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumDropdown(
    label: String,
    value: String,
    enabled: Boolean,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))

        if (!enabled) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Text(
                    text = if (value.isBlank()) "—" else value,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onSelect(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            }
        )
    }
}

@Composable
private fun ErrorInlineCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
