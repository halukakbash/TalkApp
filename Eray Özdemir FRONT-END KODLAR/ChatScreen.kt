package com.halukakbash.talk_app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.halukakbash.talk_app.data.Message
import com.halukakbash.talk_app.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.halukakbash.talk_app.viewmodel.ChatsViewModel
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    partnerId: String,
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {
    var userExists by remember { mutableStateOf(true) }
    
    LaunchedEffect(partnerId) {
        val exists = viewModel.checkUserExists(partnerId)
        if (!exists) {
            userExists = false
            navController.navigateUp()
        }
    }

    if (!userExists) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                "Chat partner no longer exists",
                modifier = Modifier.align(Alignment.Center)
            )
        }
        return
    }

    val messages by viewModel.messages.collectAsState()
    val chatPartner by viewModel.chatPartner.collectAsState()
    val currentChat by viewModel.currentChat.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(partnerId) {
        println("DEBUG: Initializing chat with: $partnerId")
        viewModel.initializeChat(partnerId)
    }

    LaunchedEffect(messages) {
        println("DEBUG: Messages updated in ChatScreen, count: ${messages.size}")
        messages.forEach { message ->
            println("DEBUG: Message in UI: ${message.content} from ${message.senderId}")
        }
    }

    LaunchedEffect(currentChat) {
        currentChat?.let { chat ->
            // Mark messages as read when chat is opened
            viewModel.markChatAsRead(chat.chatId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        chatPartner?.let { user ->
                            AsyncImage(
                                model = user.profilePhotoUrl,
                                contentDescription = "Profile photo",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "${user.name} ${user.lastName}")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        },
        bottomBar = {
            BottomAppBar(
                windowInsets = WindowInsets(0.dp)
            ) {
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
                                    println("DEBUG: Sending message: $messageText")
                                    viewModel.sendMessage(messageText.trim())
                                    messageText = ""
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
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.Bottom
        ) {
            items(messages.reversed()) { message ->
                MessageBubble(
                    message = message,
                    isFromCurrentUser = message.senderId == currentUserId,
                    onDelete = { viewModel.deleteMessage(message) }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MessageBubble(
    message: Message,
    isFromCurrentUser: Boolean,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .then(
                if (isFromCurrentUser) {
                    Modifier.combinedClickable(
                        onClick = { },
                        onLongClick = { 
                            println("DEBUG: Long press detected")
                            showDeleteDialog = true 
                        }
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isFromCurrentUser) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.content,
                    color = if (isFromCurrentUser) 
                        Color.White 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isFromCurrentUser) 
                        Color.White.copy(alpha = 0.7f) 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Message") },
            text = { Text("Are you sure you want to delete this message?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
} 