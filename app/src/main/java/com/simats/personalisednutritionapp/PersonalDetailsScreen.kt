package com.simats.personalisednutritionapp

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.personalisednutritionapp.data.AppDatabase
import com.simats.personalisednutritionapp.data.UserRepository
import com.simats.personalisednutritionapp.data.UserViewModel
import com.simats.personalisednutritionapp.ui.theme.PersonalisedNutritionAppTheme
import com.simats.personalisednutritionapp.ui.theme.PrimaryGreen

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
                    colors = listOf(PrimaryGreen, Color(0xFF81C784))
                )
            )
    ) {

        /* ---------- TOP SECTION ---------- */

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

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Personal Details",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Tell us about yourself",
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

        /* ---------- BOTTOM CARD ---------- */

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.75f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {

                Text("Full Name *", fontWeight = FontWeight.SemiBold)

                MyOutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your name") },
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Age *", fontWeight = FontWeight.SemiBold)

                MyOutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text("Gender *", fontWeight = FontWeight.SemiBold)

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

                Spacer(modifier = Modifier.height(24.dp))

                // Info Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE6F3FF),
                    border = BorderStroke(1.dp, Color(0xFFB3D9FF))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text("ðª", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "You're Starting Your Journey!",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Every great achievement starts with the decision to try. We're here to support you every step of the way",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
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
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Continue", color = Color.White)
                }
            }
        }
    }
}

/* ---------- GENDER BUTTON ---------- */

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
        shape = RoundedCornerShape(10.dp),
        border = if (selected) null else BorderStroke(1.dp, Color.LightGray),
        colors = if (selected)
            ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        else
            ButtonDefaults.outlinedButtonColors()
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Black
        )
    }
}

@Composable
fun MyOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryGreen,
            focusedLabelColor = PrimaryGreen,
            cursorColor = PrimaryGreen,
            focusedTextColor = PrimaryGreen
        )
    )
}

/* ---------- PREVIEW ---------- */

@Preview(showBackground = true)
@Composable
fun PersonalDetailsPreview() {
    PersonalisedNutritionAppTheme {
        val context = LocalContext.current
        val db = AppDatabase.getDatabase(context)
        val repository = UserRepository(db.userDao())
        PersonalDetailsScreen(
            onNavigateBack = {},
            onContinueClicked = {},
            userViewModel = UserViewModel(repository)
        )
    }
}
