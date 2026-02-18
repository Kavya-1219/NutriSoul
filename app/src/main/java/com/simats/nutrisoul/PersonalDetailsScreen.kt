package com.simats.nutrisoul

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.nutrisoul.data.UserViewModel

private val DarkGreen = Color(0xFF0F3D2E)
private val PrimaryGreen = Color(0xFF1B5E20)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsScreen(
    onNavigateBack: () -> Unit,
    onContinueClicked: () -> Unit,
    userViewModel: UserViewModel
) {
    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }

    val isContinueEnabled =
        fullName.isNotBlank() && age.isNotBlank() && selectedGender.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkGreen, PrimaryGreen)
                )
            )
    ) {

        // Top section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            Text(
                text = "Personal Details",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Let's get to know you",
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(6) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                if (index <= 0) Color.White else Color.White.copy(alpha = 0.4f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }

        // Bottom Card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.75f),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {

                Text("Full Name *", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your full name") },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = PrimaryGreen,
                        focusedLabelColor = PrimaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Age *", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = PrimaryGreen,
                        focusedLabelColor = PrimaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text("Gender *", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GenderButton(
                        text = "Male",
                        selected = selectedGender == "Male",
                        onClick = { selectedGender = "Male" },
                        modifier = Modifier.weight(1f)
                    )
                    GenderButton(
                        text = "Female",
                        selected = selectedGender == "Female",
                        onClick = { selectedGender = "Female" },
                        modifier = Modifier.weight(1f)
                    )
                    GenderButton(
                        text = "Other",
                        selected = selectedGender == "Other",
                        onClick = { selectedGender = "Other" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val user = userViewModel.user.value.copy(
                            name = fullName,
                            age = age.toIntOrNull() ?: 0,
                            gender = selectedGender
                        )
                        userViewModel.updateUser(user)
                        onContinueClicked()
                    },
                    enabled = isContinueEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        disabledContainerColor = Color(0xFF9E9E9E)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp
                    )
                ) {
                    Text("Continue", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun GenderButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
        border = if (selected) BorderStroke(2.dp, PrimaryGreen) else BorderStroke(1.dp, Color.LightGray),
        colors = if (selected) ButtonDefaults.outlinedButtonColors(containerColor = PrimaryGreen.copy(alpha = 0.1f)) else ButtonDefaults.outlinedButtonColors()
    ) {
        Text(
            text = text,
            color = if (selected) PrimaryGreen else Color.Black,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
