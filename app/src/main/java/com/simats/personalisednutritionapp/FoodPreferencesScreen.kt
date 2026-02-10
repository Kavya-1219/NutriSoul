package com.simats.personalisednutritionapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.personalisednutritionapp.composables.InfoCard
import com.simats.personalisednutritionapp.ui.theme.Green

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FoodPreferencesScreen(navController: NavController) {

    val dietTypes = listOf(
        DietType("Vegetarian", "Plant-based diet", R.drawable.vegetarian),
        DietType("Non-Vegetarian", "Includes meat & fish", R.drawable.non_vegetarian),
        DietType("Eggetarian", "Veg + Eggs", R.drawable.eggetarian),
        DietType("Vegan", "No animal products", R.drawable.vegan)
    )

    var selectedDietType by remember { mutableStateOf<DietType?>(null) }
    val allergies =
        listOf("Nuts", "Dairy/Milk", "Gluten", "Eggs", "Soy", "Shellfish", "Fish", "Peanuts")
    var selectedAllergies by remember { mutableStateOf(emptySet<String>()) }

    var foodDislikes by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Green, Color(0xFF81C784))
                )
            )
    ) {

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
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.RestaurantMenu,
                contentDescription = "Food Preferences",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Food Preferences",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Tell us about your eating habits",
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Step Indicator (Step 3)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(7) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                if (index <= 2) Color.White else Color.White.copy(alpha = 0.4f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.78f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                /* ---------------- DIET TYPE ---------------- */
                Text("Diet Type", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    dietTypes.forEach { dietType ->
                        DietTypeCard(
                            dietType = dietType,
                            selected = selectedDietType == dietType,
                            onClick = { selectedDietType = dietType }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                /* ---------------- FOOD ALLERGIES ---------------- */
                Text("Food Allergies", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Text(
                    "Select any food items you’re allergic to",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    allergies.forEach { allergy ->
                        AllergyChip(allergy, selectedAllergies.contains(allergy)) {
                            selectedAllergies = if (selectedAllergies.contains(allergy)) {
                                selectedAllergies - allergy
                            } else {
                                selectedAllergies + allergy
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                /* ---------------- FOOD DISLIKES ---------------- */
                Row {
                    Text("Food Dislikes", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Text(" (Optional)", color = Color.Gray, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                MyOutlinedTextField(
                    value = foodDislikes,
                    onValueChange = { foodDislikes = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("E.g., Bitter gourd, Mushrooms, Broccoli") },
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))

                InfoCard("We’ll exclude your allergic and dislike foods from your meal plans and suggest safe alternatives.")

                Spacer(modifier = Modifier.weight(1f))


                Button(
                    onClick = { navController.navigate("lifestyle") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green
                    )
                ) {
                    Text("Continue", color = Color.White)
                }
            }
        }
    }
}

data class DietType(val name: String, val description: String, val imageRes: Int)

@Composable
fun DietTypeCard(dietType: DietType, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Green.copy(alpha = 0.1f) else Color(0xFFF5F5F5)
        ),
        border = BorderStroke(1.dp, if (selected) Green else Color.LightGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = dietType.imageRes),
                contentDescription = dietType.name,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(dietType.name, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Text(
                dietType.description,
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AllergyChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) Green.copy(alpha = 0.1f) else Color.Transparent)
            .border(1.dp, if (selected) Green else Color.LightGray, shape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text,
            color = if (selected) Green else Color.DarkGray,
            fontWeight = FontWeight.Normal
        )
    }
}
