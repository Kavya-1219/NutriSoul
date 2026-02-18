package com.simats.nutrisoul

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

private val DarkGreen = Color(0xFF0F3D2E)
private val PrimaryGreen = Color(0xFF1B5E20)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()

    when (authState) {
        is AuthState.Authenticated -> {
            LaunchedEffect(Unit) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }
        is AuthState.Error -> {
            val error = (authState as AuthState.Error).message
            AlertDialog(
                onDismissRequest = { authViewModel.resetAuthState() },
                title = { Text("Unable to Sign In", style = MaterialTheme.typography.titleLarge) },
                text = { Text(error) },
                confirmButton = {
                    Button(
                        onClick = { authViewModel.resetAuthState() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Try Again", color = Color.White)
                    }
                }
            )
        }
        else -> Unit
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DarkGreen, PrimaryGreen)
                )
            )
    ) {
        if (authState is AuthState.Loading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 4.dp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.nutrisoul),
                contentDescription = null,
                modifier = Modifier
                    .size(95.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "NutriSoul",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 30.sp,
                    letterSpacing = 2.sp
                ),
                color = Color.White
            )

            Text(
                text = "Nourish Your Body. Elevate Your Soul.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 0.5.sp
                ),
                color = Color(0xFFD0F0D8)
            )
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp
            ),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {

            Column(modifier = Modifier.padding(24.dp)) {

                Text("Email Address", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    placeholder = { Text("Enter your email") },
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = PrimaryGreen,
                        focusedLabelColor = PrimaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Password", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    placeholder = { Text("Enter your password") },
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation =
                    if (isPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = {
                            isPasswordVisible = !isPasswordVisible
                        }) {
                            Icon(
                                if (isPasswordVisible)
                                    Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                null
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = PrimaryGreen,
                        focusedLabelColor = PrimaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        navController.navigate("forgot_password")
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Forgot Password?", color = PrimaryGreen)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { authViewModel.login(email, password) },
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
                    ),
                    enabled = authState !is AuthState.Loading
                ) {
                    Text(
                        "Login",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Don't have an account?")
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = {
                        navController.navigate("register")
                    }) {
                        Text("Create Account", color = PrimaryGreen)
                    }
                }
            }
        }
    }
}
