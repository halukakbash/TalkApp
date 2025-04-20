package com.halukakbash.talk_app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halukakbash.talk_app.R
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.NavController
import com.halukakbash.talk_app.navigation.NavigationItem
import com.halukakbash.talk_app.viewmodel.AiChatViewModel

import androidx.lifecycle.viewmodel.compose.viewModel

data class AiMessage(
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    navController: NavController,
    viewModel: AiChatViewModel = viewModel()
) {
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<AiMessage>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            "AI Chat",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 18.sp
                            )
                        ) 
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(NavigationItem.Home.route) }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.height(48.dp),
                windowInsets = WindowInsets(0.dp)
            )
        },
        bottomBar = {
            BottomAppBar {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    placeholder = { Text("Type a message") },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    // Add user message
                                    messages = messages + AiMessage(
                                        content = messageText.trim(),
                                        isFromUser = true
                                    )
                                    messageText = ""
                                    
                                    // Simulate AI response (this will be replaced with actual AI integration)
                                    messages = messages + AiMessage(
                                        content = "This is a placeholder response. The AI integration will be implemented later.",
                                        isFromUser = false
                                    )
                                }
                            },
                            enabled = messageText.isNotBlank()
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (messageText.isNotBlank()) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    },
                    maxLines = 3
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                reverseLayout = false,
                verticalArrangement = Arrangement.Top
            ) {
                // Welcome message
                if (messages.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        AiMessageBubble(
                            message = AiMessage(
                                content = "Hello! I'm your AI language assistant. How can I help you practice today?",
                                isFromUser = false
                            )
                        )
                    }
                }
                
                items(messages) { message ->
                    AiMessageBubble(message = message)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun AiMessageBubble(message: AiMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (message.isFromUser) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.content,
                    color = if (message.isFromUser) 
                        Color.White 
                    else 
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (message.isFromUser) 
                        Color.White.copy(alpha = 0.7f) 
                    else 
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
} 