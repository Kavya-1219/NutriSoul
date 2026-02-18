package com.simats.nutrisoul

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NavigationItem(val title: String, val icon: ImageVector)

@Composable
fun BottomNavigationBar(onItemSelected: (Int) -> Unit) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf(
        NavigationItem("Home", Icons.Default.Home),
        NavigationItem("Stress & Sleep", Icons.Default.Psychology),
        NavigationItem("Recipes", Icons.Default.RestaurantMenu),
        NavigationItem("Insights", Icons.Default.BarChart),
        NavigationItem("Settings", Icons.Default.Settings)
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title, fontSize = 10.sp) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    onItemSelected(index)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color(0xFF8E8E93),
                    unselectedTextColor = Color(0xFF8E8E93)
                )
            )
        }
    }
}