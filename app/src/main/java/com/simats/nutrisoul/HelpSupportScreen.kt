@file:OptIn(ExperimentalMaterial3Api::class)

package com.simats.nutrisoul

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

private val faqs = listOf(
    "How do I log my meals?" to "Go to the 'Log Food' screen from the home page. You can search for foods from our database, add manually by entering details, or scan/upload food images for automatic nutrition detection.",
    "How accurate are the calorie calculations?" to "Our calorie calculations are based on scientifically validated formulas (Mifflin-St Jeor equation) and take into account your age, gender, weight, height, and activity level. For best results, update your profile regularly.",
    "Can I customize my meal plan?" to "Yes! Go to the Meal Plan screen and tap the edit button on any meal. You can adjust portions, remove items, or generate a completely new plan by tapping the refresh button.",
    "How do I track water intake?" to "Use the water tracking widget on the home screen. Tap the + button each time you drink a glass of water (250ml). Your daily goal is calculated based on your body weight.",
    "What if I have food allergies?" to "Your food allergies are already saved from your profile setup. The meal plan automatically excludes all your allergic foods. You can update allergies anytime in Profile Settings.",
    "How do I change my goal?" to "Go to Profile Settings and edit your goal. The app will recalculate your daily calorie target and adjust meal recommendations accordingly.",
)

private data class Message(val text: String, val isUser: Boolean, val timestamp: String)

@Composable
fun HelpSupportScreen(navController: NavController) {
    var activeTab by remember { mutableStateOf("faqs") }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header(navController)
            TabSwitcher(activeTab) { activeTab = it }

            when (activeTab) {
                "faqs" -> FaqsTab { activeTab = "chat" }
                "chat" -> ChatTab()
            }
        }
    }
}

@Composable
private fun Header(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFA726), Color(0xFFF4511E))
                )
            )
            .padding(16.dp)
    ) {
        Column {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HelpOutline, contentDescription = "Help", tint = Color.White, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Help & Support", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Text(text = "We're here to help you succeed", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

@Composable
private fun TabSwitcher(activeTab: String, onTabSelected: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            Button(
                onClick = { onTabSelected("faqs") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == "faqs") MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (activeTab == "faqs") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("FAQs")
            }
            Button(
                onClick = { onTabSelected("chat") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == "chat") MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (activeTab == "chat") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Chat")
                }
            }
        }
    }
}

@Composable
fun FaqsTab(onChatClick: () -> Unit) {
    var expandedFaq by remember { mutableStateOf<Int?>(null) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(faqs) { index, faq ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                onClick = { expandedFaq = if (expandedFaq == index) null else index }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = faq.first,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expandedFaq == index) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand"
                        )
                    }
                    AnimatedVisibility(visible = expandedFaq == index) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = faq.second, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Still need help?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Can't find what you're looking for? Try our AI chat or contact support.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onChatClick) {
                        Text("Chat with AI")
                    }
                }
            }
        }
    }
}

@Composable
fun ChatTab() {
    val keyboardController = LocalSoftwareKeyboardController.current
    val chatMessages = remember {
        mutableStateListOf(
            Message("Hi! I'm your AI nutrition assistant. How can I help you today?", false, SimpleDateFormat("hh:mm a", Locale.US).format(Date()))
        )
    }
    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages.size) {
        listState.animateScrollToItem(chatMessages.size - 1)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(chatMessages) { message ->
                MessageBubble(message)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                placeholder = { Text("Ask me anything...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions {
                    if (inputMessage.isNotBlank()) {
                        chatMessages.add(Message(inputMessage, true, SimpleDateFormat("hh:mm a", Locale.US).format(Date())))
                        inputMessage = ""
                        keyboardController?.hide()
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (inputMessage.isNotBlank()) {
                        chatMessages.add(Message(inputMessage, true, SimpleDateFormat("hh:mm a", Locale.US).format(Date())))
                        inputMessage = ""
                        keyboardController?.hide()
                    }
                },
                shape = CircleShape,
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.last().isUser) {
            delay(1000)
            val response = generateAIResponse(chatMessages.last().text)
            chatMessages.add(Message(response, false, SimpleDateFormat("hh:mm a", Locale.US).format(Date())))
        }
    }
}

@Composable
private fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = if (message.isUser) 16.dp else 0.dp,
                topEnd = if (message.isUser) 0.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    color = if (message.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = (if (message.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                )
            }
        }
    }
}

private fun generateAIResponse(userInput: String): String {
    val input = userInput.lowercase()
    return when {
        "hello" in input || "hi" in input -> "Hello there! How can I assist you with your nutrition goals today?"
        "calorie" in input -> "Based on general guidelines, a moderately active adult needs about 2000-2500 calories. For a personalized calculation, please provide your age, gender, weight, height, and activity level in your profile."
        "lose weight" in input -> "To lose weight, you should aim for a consistent calorie deficit, focus on high-protein meals, and incorporate regular exercise. Strength training is highly effective!"
        "meal" in input || "food" in input -> "For a healthy meal, I'd suggest something balanced. For example, a grilled chicken salad for lunch, or quinoa with roasted vegetables for dinner. Do you have any dietary preferences?"
        "protein" in input -> "A good target for protein intake is around 1.6-2.2 grams per kilogram of body weight, especially if you are active. Good sources include chicken, fish, eggs, tofu, and lentils."
        else -> "I can help with questions about nutrition, calories, meal planning, and exercise. What's on your mind?"
    }
}
