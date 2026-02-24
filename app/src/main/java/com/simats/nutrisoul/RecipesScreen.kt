
package com.simats.nutrisoul

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController

data class Recipe(
    val id: Int,
    val name: String,
    val category: String,
    val cookTime: String,
    val calories: Int,
    val servings: Int,
    val difficulty: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val fiber: Int,
    val image: String
)

val recipes = listOf(
    Recipe(
        id = 1,
        name = "Masala Oats Upma",
        category = "breakfast",
        cookTime = "15 mins",
        calories = 180,
        servings = 2,
        difficulty = "Easy",
        protein = 6,
        carbs = 28,
        fats = 4,
        fiber = 5,
        image = "🥣",
        ingredients = listOf(
            "1 cup rolled oats",
            "1 onion, chopped",
            "1 tomato, chopped",
            "1 green chili, chopped",
            "1/2 tsp mustard seeds",
            "Curry leaves",
            "1/2 tsp turmeric",
            "Salt to taste",
            "2 cups water",
            "Fresh coriander"
        ),
        instructions = listOf(
            "Dry roast oats in a pan for 3-4 minutes until fragrant. Set aside.",
            "Heat 1 tsp oil, add mustard seeds and curry leaves.",
            "Add onions, green chili, and sauté until golden.",
            "Add tomatoes, turmeric, and salt. Cook for 2 minutes.",
            "Add 2 cups water and bring to boil.",
            "Add roasted oats, mix well, and cook for 3-4 minutes.",
            "Garnish with coriander and serve hot."
        )
    ),
    Recipe(
        id = 2,
        name = "Moong Dal Cheela",
        category = "breakfast",
        cookTime = "20 mins",
        calories = 220,
        servings = 2,
        difficulty = "Easy",
        protein = 12,
        carbs = 30,
        fats = 5,
        fiber = 8,
        image = "🥞",
        ingredients = listOf(
            "1 cup moong dal (soaked 4 hours)",
            "1 onion, chopped",
            "1 green chili",
            "1 inch ginger",
            "1/2 tsp cumin seeds",
            "Salt to taste",
            "Fresh coriander",
            "Oil for cooking"
        ),
        instructions = listOf(
            "Drain soaked moong dal and grind to a smooth batter with ginger and green chili.",
            "Add chopped onions, cumin seeds, salt, and coriander. Mix well.",
            "Add water if needed to make a pancake-like consistency.",
            "Heat a non-stick pan and grease lightly.",
            "Pour a ladle of batter and spread in a circle.",
            "Cook on medium heat until golden, flip and cook other side.",
            "Serve hot with green chutney or yogurt."
        )
    ),
    Recipe(
        id = 3,
        name = "Vegetable Poha",
        category = "breakfast",
        cookTime = "15 mins",
        calories = 200,
        servings = 2,
        difficulty = "Easy",
        protein = 4,
        carbs = 35,
        fats = 6,
        fiber = 3,
        image = "🍚",
        ingredients = listOf(
            "2 cups flattened rice (poha)",
            "1 onion, sliced",
            "1 potato, diced small",
            "1/2 cup peas",
            "1 green chili, chopped",
            "1/2 tsp mustard seeds",
            "1/2 tsp turmeric",
            "Curry leaves",
            "Peanuts",
            "Lemon juice",
            "Fresh coriander"
        ),
        instructions = listOf(
            "Rinse poha in water and drain immediately. Set aside.",
            "Heat 1 tbsp oil, add mustard seeds, curry leaves, and peanuts.",
            "Add onions, green chili, and sauté until translucent.",
            "Add potatoes and peas, cook until tender (add water if needed).",
            "Add turmeric and salt, mix well.",
            "Add drained poha, mix gently without breaking.",
            "Cook for 2-3 minutes.",
            "Add lemon juice, garnish with coriander, and serve."
        )
    ),
    Recipe(
        id = 4,
        name = "Dal Tadka",
        category = "lunch",
        cookTime = "30 mins",
        calories = 180,
        servings = 4,
        difficulty = "Medium",
        protein = 10,
        carbs = 28,
        fats = 4,
        fiber = 12,
        image = "🍲",
        ingredients = listOf(
            "1 cup toor dal (split pigeon peas)",
            "2 tomatoes, chopped",
            "1 onion, chopped",
            "2 green chilies",
            "1 tsp cumin seeds",
            "3-4 garlic cloves",
            "1/2 tsp turmeric",
            "1 tsp red chili powder",
            "1 tsp garam masala",
            "Fresh coriander",
            "Ghee for tadka"
        ),
        instructions = listOf(
            "Wash and pressure cook dal with turmeric, salt, and 3 cups water for 3-4 whistles.",
            "Mash the cooked dal lightly.",
            "Heat 2 tsp ghee in a pan, add cumin seeds and garlic.",
            "Add onions and green chilies, sauté until golden.",
            "Add tomatoes, red chili powder, and cook until mushy.",
            "Pour this tadka over the dal and mix well.",
            "Add garam masala and simmer for 5 minutes.",
            "Garnish with coriander and serve with rice or roti."
        )
    ),
    Recipe(
        id = 5,
        name = "Paneer Tikka Masala",
        category = "lunch",
        cookTime = "35 mins",
        calories = 320,
        servings = 3,
        difficulty = "Medium",
        protein = 18,
        carbs = 12,
        fats = 22,
        fiber = 3,
        image = "🧊",
        ingredients = listOf(
            "250g paneer, cubed",
            "1 cup yogurt",
            "1 tbsp tikka masala",
            "2 tomatoes, pureed",
            "1 onion, chopped",
            "1 capsicum, cubed",
            "1 inch ginger-garlic paste",
            "1 tsp kasuri methi",
            "1/2 cup cream",
            "Oil for cooking"
        ),
        instructions = listOf(
            "Marinate paneer and capsicum with yogurt, tikka masala, and salt for 30 minutes.",
            "Grill or pan-fry marinated paneer until golden. Set aside.",
            "Heat oil, add ginger-garlic paste and onions. Sauté until golden.",
            "Add tomato puree, salt, and cook until oil separates.",
            "Add grilled paneer, kasuri methi, and mix gently.",
            "Add cream and simmer for 5 minutes.",
            "Garnish with coriander and serve with naan or rice."
        )
    ),
    Recipe(
        id = 6,
        name = "Rajma Masala (Kidney Bean Curry)",
        category = "lunch",
        cookTime = "40 mins",
        calories = 240,
        servings = 4,
        difficulty = "Medium",
        protein = 14,
        carbs = 38,
        fats = 4,
        fiber = 11,
        image = "🫘",
        ingredients = listOf(
            "1 cup rajma (kidney beans), soaked overnight",
            "2 onions, chopped",
            "2 tomatoes, pureed",
            "1 tbsp ginger-garlic paste",
            "2 green chilies",
            "1 tsp cumin seeds",
            "1 tsp coriander powder",
            "1 tsp garam masala",
            "1/2 tsp red chili powder",
            "Fresh coriander"
        ),
        instructions = listOf(
            "Pressure cook soaked rajma with salt and water for 6-7 whistles until soft.",
            "Heat oil, add cumin seeds and ginger-garlic paste.",
            "Add onions and green chilies, sauté until golden brown.",
            "Add tomato puree, coriander powder, red chili powder, and cook well.",
            "Add cooked rajma with its water.",
            "Simmer for 15-20 minutes until gravy thickens.",
            "Add garam masala, garnish with coriander.",
            "Serve hot with rice."
        )
    ),
    Recipe(
        id = 7,
        name = "Sprouts Chaat",
        category = "snack",
        cookTime = "10 mins",
        calories = 150,
        servings = 2,
        difficulty = "Easy",
        protein = 8,
        carbs = 22,
        fats = 3,
        fiber = 6,
        image = "🥗",
        ingredients = listOf(
            "2 cups mixed sprouts (moong, chana)",
            "1 onion, chopped",
            "1 tomato, chopped",
            "1 cucumber, chopped",
            "1 green chili, chopped",
            "Chaat masala",
            "Lemon juice",
            "Fresh coriander",
            "Sev (optional)"
        ),
        instructions = listOf(
            "Boil sprouts for 5 minutes until tender. Drain and cool.",
            "In a bowl, mix sprouts, onions, tomatoes, and cucumber.",
            "Add green chili, chaat masala, and salt.",
            "Squeeze lemon juice and mix well.",
            "Garnish with coriander and sev.",
            "Serve immediately as a healthy snack."
        )
    ),
    Recipe(
        id = 8,
        name = "Masala Roasted Makhana",
        category = "snack",
        cookTime = "10 mins",
        calories = 120,
        servings = 2,
        difficulty = "Easy",
        protein = 4,
        carbs = 18,
        fats = 4,
        fiber = 2,
        image = "🍿",
        ingredients = listOf(
            "2 cups makhana (fox nuts)",
            "1 tsp ghee",
            "1/2 tsp chaat masala",
            "1/4 tsp turmeric",
            "1/4 tsp red chili powder",
            "Salt to taste",
            "Curry leaves (optional)"
        ),
        instructions = listOf(
            "Heat ghee in a pan on low flame.",
            "Add makhana and roast for 5-7 minutes, stirring continuously.",
            "Add curry leaves if using.",
            "Once makhana becomes crispy, add all spices and salt.",
            "Mix well to coat evenly.",
            "Let it cool and store in an airtight container.",
            "Enjoy as a healthy, crunchy snack."
        )
    ),
    Recipe(
        id = 9,
        name = "Vegetable Cutlets",
        category = "snack",
        cookTime = "25 mins",
        calories = 180,
        servings = 4,
        difficulty = "Medium",
        protein = 6,
        carbs = 26,
        fats = 6,
        fiber = 4,
        image = "🥔",
        ingredients = listOf(
            "3 potatoes, boiled and mashed",
            "1/2 cup mixed vegetables (carrots, peas, beans)",
            "2 tbsp bread crumbs",
            "1 green chili, chopped",
            "1 tsp ginger paste",
            "1/2 tsp garam masala",
            "Fresh coriander",
            "Salt to taste",
            "Oil for shallow frying"
        ),
        instructions = listOf(
            "Mix mashed potatoes with boiled vegetables, green chili, ginger, and spices.",
            "Add coriander and bread crumbs, mix well.",
            "Shape into round or oval patties.",
            "Heat oil in a pan.",
            "Shallow fry cutlets until golden brown on both sides.",
            "Drain on paper towels.",
            "Serve hot with green chutney or ketchup."
        )
    ),
    Recipe(
        id = 10,
        name = "Palak Paneer",
        category = "dinner",
        cookTime = "30 mins",
        calories = 280,
        servings = 3,
        difficulty = "Medium",
        protein = 16,
        carbs = 10,
        fats = 20,
        fiber = 4,
        image = "🥬",
        ingredients = listOf(
            "250g paneer, cubed",
            "400g spinach (palak)",
            "1 onion, chopped",
            "2 tomatoes, chopped",
            "1 tbsp ginger-garlic paste",
            "2 green chilies",
            "1 tsp cumin seeds",
            "1/2 tsp garam masala",
            "2 tbsp cream",
            "Salt to taste"
        ),
        instructions = listOf(
            "Blanch spinach in boiling water for 2 minutes, then put in cold water.",
            "Blend spinach with green chilies to a smooth puree.",
            "Heat oil, add cumin seeds and ginger-garlic paste.",
            "Add onions and sauté until golden.",
            "Add tomatoes and cook until mushy.",
            "Add spinach puree, salt, and cook for 5 minutes.",
            "Add paneer cubes and garam masala, simmer for 5 minutes.",
            "Add cream, mix gently, and serve with roti."
        )
    ),
    Recipe(
        id = 11,
        name = "Chicken Curry",
        category = "dinner",
        cookTime = "40 mins",
        calories = 320,
        servings = 4,
        difficulty = "Medium",
        protein = 35,
        carbs = 8,
        fats = 18,
        fiber = 2,
        image = "🍗",
        ingredients = listOf(
            "500g chicken, cut into pieces",
            "2 onions, chopped",
            "2 tomatoes, pureed",
            "1 tbsp ginger-garlic paste",
            "1 tsp cumin seeds",
            "2 tsp coriander powder",
            "1 tsp garam masala",
            "1/2 tsp turmeric",
            "1 tsp red chili powder",
            "Curry leaves",
            "Fresh coriander"
        ),
        instructions = listOf(
            "Heat oil, add cumin seeds and curry leaves.",
            "Add onions and sauté until golden brown.",
            "Add ginger-garlic paste and sauté for 1 minute.",
            "Add tomato puree and all spices, cook until oil separates.",
            "Add chicken pieces and mix well to coat with masala.",
            "Add 1 cup water, salt, and cover. Cook for 25-30 minutes until chicken is tender.",
            "Garnish with coriander and serve with rice or roti."
        )
    ),
    Recipe(
        id = 12,
        name = "Vegetable Khichdi",
        category = "dinner",
        cookTime = "25 mins",
        calories = 210,
        servings = 3,
        difficulty = "Easy",
        protein = 8,
        carbs = 38,
        fats = 4,
        fiber = 6,
        image = "🍛",
        ingredients = listOf(
            "1/2 cup rice",
            "1/2 cup moong dal",
            "Mixed vegetables (carrots, peas, beans)",
            "1 tsp cumin seeds",
            "1/2 tsp turmeric",
            "1 tsp ginger, chopped",
            "2 green chilies",
            "Salt to taste",
            "Ghee for tempering"
        ),
        instructions = listOf(
            "Wash rice and dal together.",
            "Heat 1 tsp ghee, add cumin seeds and ginger.",
            "Add vegetables and sauté for 2 minutes.",
            "Add rice, dal, turmeric, salt, and 4 cups water.",
            "Pressure cook for 3-4 whistles until soft and mushy.",
            "Make a tadka with ghee, cumin, and green chilies.",
            "Pour over khichdi and serve with yogurt or pickle."
        )
    )
)

@Composable
fun RecipesScreen(navController: NavController) {
    var selectedCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var favorites by remember { mutableStateOf(setOf<Int>()) }

    val categories = listOf(
        "all" to "🍽️ All",
        "breakfast" to "🌅 Breakfast",
        "lunch" to "☀️ Lunch",
        "snack" to "🍪 Snacks",
        "dinner" to "🌙 Dinner"
    )

    val filteredRecipes = recipes.filter {
        (selectedCategory == "all" || it.category == selectedCategory) &&
                it.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF0F2F5))
        ) {
            Header(searchQuery, onSearchQueryChange = { searchQuery = it })
            CategoryTabs(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
            RecipesGrid(
                recipes = filteredRecipes,
                favorites = favorites,
                onRecipeClick = { selectedRecipe = it },
                onToggleFavorite = {
                    favorites = if (favorites.contains(it)) {
                        favorites - it
                    } else {
                        favorites + it
                    }
                }
            )
        }

        if (selectedRecipe != null) {
            RecipeDetailDialog(
                recipe = selectedRecipe!!,
                isFavorite = favorites.contains(selectedRecipe!!.id),
                onDismiss = { selectedRecipe = null },
                onToggleFavorite = {
                    favorites = if (favorites.contains(it)) {
                        favorites - it
                    } else {
                        favorites + it
                    }
                }
            )
        }
    }
}

@Composable
private fun Header(searchQuery: String, onSearchQueryChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFA726), Color(0xFFFF9800))
                )
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.FoodBank,
                contentDescription = "Recipes",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Recipes",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Healthy Indian recipes for every meal",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search recipes...", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,

            )
        )
    }
}

@Composable
private fun CategoryTabs(
    categories: List<Pair<String, String>>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val selectedTabIndex = categories.indexOfFirst { it.first == selectedCategory }

    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.White,
        contentColor = Color.Black,
        indicator = { tabPositions ->
            if (selectedTabIndex != -1) {
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Color(0xFFFFB74D)
                )
            }
        }
    ) {
        categories.forEachIndexed { index, (id, label) ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onCategorySelected(id) },
                text = { Text(label) }
            )
        }
    }
}

@Composable
private fun RecipesGrid(
    recipes: List<Recipe>,
    favorites: Set<Int>,
    onRecipeClick: (Recipe) -> Unit,
    onToggleFavorite: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(recipes) { recipe ->
            RecipeCard(
                recipe = recipe,
                isFavorite = favorites.contains(recipe.id),
                onClick = { onRecipeClick(recipe) },
                onToggleFavorite = { onToggleFavorite(recipe.id) }
            )
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: Recipe,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = recipe.image, fontSize = 56.sp)
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = recipe.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "Cook Time",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = recipe.cookTime, fontSize = 12.sp, color = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Whatshot,
                        contentDescription = "Calories",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${recipe.calories} kcal", fontSize = 12.sp, color = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = "Servings",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${recipe.servings} servings", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recipe.difficulty,
                    color = if (recipe.difficulty == "Easy") Color(0xFF388E3C) else Color(0xFFF57C00),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (recipe.difficulty == "Easy") Color(0xFFC8E6C9) else Color(
                                0xFFFFE0B2
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun RecipeDetailDialog(
    recipe: Recipe,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: (Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFA726), Color(0xFFFF9800))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = recipe.image, fontSize = 100.sp)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(recipe.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InfoChip("Cook Time", recipe.cookTime, Icons.Default.Schedule)
                        InfoChip("Calories", "${recipe.calories} kcal", Icons.Default.Whatshot)
                        InfoChip("Servings", "${recipe.servings} servings", Icons.Default.Group)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    NutritionInfo(recipe)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Ingredients", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            recipe.ingredients.forEach {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.FiberManualRecord,
                                        contentDescription = null,
                                        modifier = Modifier.size(8.dp),
                                        tint = Color(0xFFFFB74D)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(it)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Instructions", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    recipe.instructions.forEachIndexed { index, instruction ->
                        Row(modifier = Modifier.padding(bottom = 8.dp)) {
                            Text(
                                text = "${index + 1}.",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB74D),
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(instruction)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onToggleFavorite(recipe.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFavorite) Color.Red else Color(0xFFFFB74D)
                        )
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = Color(0xFFFFB74D))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
private fun NutritionInfo(recipe: Recipe) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NutritionChip("Protein", "${recipe.protein}g", Color(0xFF42A5F5))
            NutritionChip("Carbs", "${recipe.carbs}g", Color(0xFF26A69A))
            NutritionChip("Fats", "${recipe.fats}g", Color(0xFF7E57C2))
            NutritionChip("Fiber", "${recipe.fiber}g", Color(0xFF66BB6A))
        }
    }
}

@Composable
private fun NutritionChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}
