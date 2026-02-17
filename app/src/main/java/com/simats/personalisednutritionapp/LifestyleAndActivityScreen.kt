package com.simats.personalisednutritionapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.personalisednutritionapp.data.UserViewModel
import com.simats.personalisednutritionapp.ui.theme.PrimaryGreen

@Composable
fun LifestyleAndActivityScreen(navController: NavController, userViewModel: UserViewModel) {

    val activityLevels = listOf(
        ActivityLevel("Sedentary", "Little or no exercise, desk job", "Examples: Office work, studying, minimal movement"),
        ActivityLevel("Lightly Active", "Light exercise 1-3 days/week", "Examples: Walking, light housework, casual cycling"),
        ActivityLevel("Moderately Active", "Moderate exercise 3-5 days/week", "Examples: Regular gym, sports, active job"),
        ActivityLevel("Very Active", "Hard exercise 6-7 days/week", "Examples: Intense training, athletic activities")
    )

    var selectedLevel by remember { mutableStateOf<ActivityLevel?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(PrimaryGreen, Color(0xFF81C784))
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.MonitorHeart,
                contentDescription = "Lifestyle & Activity",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Lifestyle & Activity",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "How active are you in daily life?",
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Step Indicator (Step 4)
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
                                if (index <= 3) Color.White else Color.White.copy(alpha = 0.4f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text("Select your typical activity level", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    activityLevels.forEach { level ->
                        ActivityLevelCard(
                            level = level,
                            isSelected = selectedLevel == level,
                            onClick = { selectedLevel = level }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                InfoSection()

                Spacer(modifier = Modifier.height(16.dp))

                 Button(
                    onClick = {
                        selectedLevel?.let {
                            userViewModel.updateUser(userViewModel.user.value.copy(activityLevel = it.title))
                        }
                        navController.navigate(Screen.Goals.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    enabled = selectedLevel != null
                ) {
                    Text("Continue to Goals", color = Color.White)
                }
            }
        }
    }
}

data class ActivityLevel(val title: String, val description: String, val examples: String)

@Composable
fun ActivityLevelCard(level: ActivityLevel, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) PrimaryGreen else Color.LightGray
    val containerColor = if (isSelected) PrimaryGreen.copy(alpha = 0.05f) else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = getActivityLevelIcon(level.title)),
                contentDescription = level.title,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.size(16.dp))
            Column {
                Text(level.title, fontWeight = FontWeight.Bold)
                Text(level.description, color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(level.examples, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun InfoSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_bulb),
            contentDescription = "Info",
            tint = Color(0xFF0D47A1)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Your activity level helps us calculate your daily calorie burn (TDEE) and adjust your nutrition plan accordingly.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF0D47A1)
        )
    }
}

private fun getActivityLevelIcon(level: String): Int {
    return when (level) {
        "Sedentary" -> R.drawable.la1
        "Lightly Active" -> R.drawable.la2
        "Moderately Active" -> R.drawable.la3
        "Very Active" -> R.drawable.la4
        else -> R.drawable.la1
    }
}
