package com.simats.nutrisoul

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.nutrisoul.data.User
import com.simats.nutrisoul.data.UserViewModel
import com.simats.nutrisoul.ui.theme.Gray50
import com.simats.nutrisoul.ui.theme.Gray900
import com.simats.nutrisoul.ui.theme.Green400
import com.simats.nutrisoul.ui.theme.Green600

@Composable
fun ProfileScreen(navController: NavController, userViewModel: UserViewModel) {
    val user by userViewModel.user.collectAsState()
    val darkMode by userViewModel.darkMode.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var showPasswordChange by remember { mutableStateOf(false) }

    Scaffold {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(if (darkMode) Gray900 else Gray50)
                .padding(it)
        ) {
            item {
                ProfileHeader(navController, isEditing, onEditClick = { isEditing = !isEditing }, user = user)
            }

            item {
                user?.let { currentUser ->
                    ProfileContent(
                        user = currentUser,
                        isEditing = isEditing,
                        showPasswordChange = showPasswordChange,
                        onPasswordChangeClick = { showPasswordChange = !showPasswordChange },
                        onSaveChanges = {
                            userViewModel.updateUser(it)
                            isEditing = false
                        },
                        onCancelChanges = {
                            isEditing = false
                        },
                        darkMode = darkMode
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(navController: NavController, isEditing: Boolean, onEditClick: () -> Unit, user: User?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = Brush.verticalGradient(colors = listOf(Green600, Green400))
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Profile", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            if (!isEditing) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                }
            } else {
                Box(modifier = Modifier.size(48.dp)) // Placeholder for spacing
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 50.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = "User", tint = Green600, modifier = Modifier.size(60.dp))
            }
        }
    }
}

@Composable
fun ProfileContent(
    user: User,
    isEditing: Boolean,
    showPasswordChange: Boolean,
    onPasswordChangeClick: () -> Unit,
    onSaveChanges: (User) -> Unit,
    onCancelChanges: () -> Unit,
    darkMode: Boolean
) {
    var editableUser by remember { mutableStateOf(user) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.padding(top = 60.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {

        EditableTextField(label = "Name", value = editableUser.name, onValueChange = { editableUser = editableUser.copy(name = it) }, isEditing = isEditing, darkMode = darkMode)
        EditableTextField(label = "Age", value = editableUser.age.toString(), onValueChange = { editableUser = editableUser.copy(age = it.toIntOrNull() ?: 0) }, isEditing = isEditing, keyboardType = KeyboardType.Number, darkMode = darkMode)
        EditableTextField(label = "Gender", value = editableUser.gender, onValueChange = { editableUser = editableUser.copy(gender = it) }, isEditing = isEditing, darkMode = darkMode)
        EditableTextField(label = "Height (cm)", value = editableUser.height.toString(), onValueChange = { editableUser = editableUser.copy(height = it.toFloatOrNull() ?: 0f) }, isEditing = isEditing, keyboardType = KeyboardType.Number, darkMode = darkMode)
        EditableTextField(label = "Weight (kg)", value = editableUser.weight.toString(), onValueChange = { editableUser = editableUser.copy(weight = it.toFloatOrNull() ?: 0f) }, isEditing = isEditing, keyboardType = KeyboardType.Number, darkMode = darkMode)

        if (isEditing) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancelChanges) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onSaveChanges(editableUser) }) { Text("Save") }
            }
        } else {
            Button(onClick = onPasswordChangeClick, modifier = Modifier.fillMaxWidth()) {
                Text(if (showPasswordChange) "Hide Password Change" else "Change Password")
            }
        }

        if (showPasswordChange) {
            Spacer(modifier = Modifier.height(16.dp))
            PasswordField(label = "Current Password", value = currentPassword, onValueChange = { currentPassword = it }, darkMode = darkMode)
            PasswordField(label = "New Password", value = newPassword, onValueChange = { newPassword = it }, darkMode = darkMode)
            PasswordField(label = "Confirm Password", value = confirmPassword, onValueChange = { confirmPassword = it }, darkMode = darkMode)
            passwordError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Button(onClick = {
                // Implement password change logic
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Update Password")
            }
        }
    }
}

@Composable
fun EditableTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditing: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    darkMode: Boolean
) {
    val textColor = if (darkMode) Color.White else Color.Black
    if (isEditing) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = textColor) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Green600,
                unfocusedBorderColor = Color.Gray,
                cursorColor = Green600
            )
        )
    } else {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Text(value, fontSize = 16.sp, color = textColor)
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun PasswordField(label: String, value: String, onValueChange: (String) -> Unit, darkMode: Boolean) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = if (darkMode) Color.White else Color.Black) },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (passwordVisible)
                Icons.Filled.Visibility
            else Icons.Filled.VisibilityOff

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(image, "toggle password visibility")
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Green600,
            unfocusedBorderColor = Color.Gray,
            cursorColor = Green600
        )
    )
    Spacer(modifier = Modifier.height(8.dp))
}
