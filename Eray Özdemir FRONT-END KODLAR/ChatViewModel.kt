package com.halukakbash.talk_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.halukakbash.talk_app.data.Chat
import com.halukakbash.talk_app.data.ChatPreview
import com.halukakbash.talk_app.data.Message
import com.halukakbash.talk_app.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _currentChat = MutableStateFlow<ChatPreview?>(null)
    val currentChat: StateFlow<ChatPreview?> = _currentChat

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _chatPartner = MutableStateFlow<User?>(null)
    val chatPartner: StateFlow<User?> = _chatPartner

    fun initializeChat(otherUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // Get or create chat document
                val chatId = getChatId(currentUserId, otherUserId)
                
                // Set chat partner
                val otherUser = firestore.collection("users")
                    .document(otherUserId)
                    .get()
                    .await()
                    .toObject(User::class.java)

                _chatPartner.value = otherUser

                otherUser?.let { user ->
                    _currentChat.value = ChatPreview(
                        chatId = chatId,
                        otherUser = user,
                        lastMessage = "",
                        lastMessageTime = System.currentTimeMillis(),
                        lastMessageSenderId = "",
                        unreadCount = 0
                    )
                }
                
                // Start listening to messages
                listenToMessages(chatId)
                
                println("DEBUG: Chat initialized with ID: $chatId")
                println("DEBUG: Chat partner: ${_chatPartner.value?.name}")
            } catch (e: Exception) {
                println("DEBUG: Error initializing chat: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun getChatId(currentUserId: String, otherUserId: String): String {
        // Check if a chat already exists between these users
        val existingChat = firestore.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .get()
            .await()
            .documents
            .firstOrNull { chat ->
                val participants = chat.get("participants") as? List<String>
                participants?.containsAll(listOf(currentUserId, otherUserId)) == true
            }

        return existingChat?.id ?: createNewChat(currentUserId, otherUserId)
    }

    private suspend fun createNewChat(currentUserId: String, otherUserId: String): String {
        println("DEBUG: Creating new chat between $currentUserId and $otherUserId")
        
        val timestamp = System.currentTimeMillis()
        
        // Create a complete chat document with all required fields
        val chatDocument = mapOf(
            "participants" to listOf(currentUserId, otherUserId),
            "lastMessage" to "",
            "lastMessageTime" to timestamp,
            "lastMessageSenderId" to "",
            "createdAt" to timestamp,
            "lastRead_$currentUserId" to timestamp,
            "lastRead_$otherUserId" to 0L
        )
        
        // Create the chat document first
        val chatRef = firestore.collection("chats")
            .add(chatDocument)
            .await()

        println("DEBUG: Created new chat with ID: ${chatRef.id}")
        return chatRef.id
    }

    private fun listenToMessages(chatId: String) {
        println("DEBUG: Starting message listener for chat: $chatId")
        
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("DEBUG: Error in message listener: ${error.message}")
                    return@addSnapshotListener
                }

                try {
                    val messages = snapshot?.documents?.mapNotNull { doc ->
                        println("DEBUG: Processing message: ${doc.id}")
                        println("DEBUG: Message data: ${doc.data}")
                        
                        Message(
                            id = doc.getString("id") ?: doc.id,
                            chatId = doc.getString("chatId") ?: "",
                            senderId = doc.getString("senderId") ?: "",
                            content = doc.getString("content") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            isRead = doc.getBoolean("isRead") ?: false
                        )
                    } ?: emptyList()

                    _messages.value = messages
                    println("DEBUG: Updated messages list. Count: ${messages.size}")
                } catch (e: Exception) {
                    println("DEBUG: Error processing messages: ${e.message}")
                    e.printStackTrace()
                }
            }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch
                val currentChatId = _currentChat.value?.chatId ?: return@launch
                val timestamp = System.currentTimeMillis()

                // Create and save message
                val messageRef = firestore.collection("chats")
                    .document(currentChatId)
                    .collection("messages")
                    .document()

                val messageData = mapOf(
                    "id" to messageRef.id,
                    "chatId" to currentChatId,
                    "senderId" to currentUserId,
                    "content" to content,
                    "timestamp" to timestamp,
                    "isRead" to false
                )

                messageRef.set(messageData).await()

                // Update chat document
                val chatUpdate = mapOf(
                    "lastMessage" to content,
                    "lastMessageTime" to timestamp,
                    "lastMessageSenderId" to currentUserId,
                    "participants" to _currentChat.value?.let { listOf(currentUserId, it.otherUser.id) }
                )

                firestore.collection("chats")
                    .document(currentChatId)
                    .update(chatUpdate)
                    .await()

            } catch (e: Exception) {
                println("DEBUG: Error sending message: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun deleteMessage(message: Message) {
        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch
                
                // Only allow users to delete their own messages
                if (message.senderId != currentUserId) {
                    return@launch
                }

                // Delete the message
                firestore.collection("chats")
                    .document(message.chatId)
                    .collection("messages")
                    .document(message.id)
                    .delete()
                    .await()

                // Update the chat's last message if this was the last message
                val chat = _currentChat.value ?: return@launch
                if (chat.lastMessage == message.content) {
                    // Get the previous message
                    val previousMessage = firestore.collection("chats")
                        .document(message.chatId)
                        .collection("messages")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .await()
                        .documents
                        .firstOrNull()

                    // Update chat document with previous message info or empty if no messages left
                    val chatUpdate = if (previousMessage != null) {
                        mapOf(
                            "lastMessage" to (previousMessage.getString("content") ?: ""),
                            "lastMessageTime" to (previousMessage.getLong("timestamp") ?: System.currentTimeMillis()),
                            "lastMessageSenderId" to (previousMessage.getString("senderId") ?: "")
                        )
                    } else {
                        mapOf(
                            "lastMessage" to "",
                            "lastMessageTime" to System.currentTimeMillis(),
                            "lastMessageSenderId" to ""
                        )
                    }

                    firestore.collection("chats")
                        .document(message.chatId)
                        .update(chatUpdate)
                        .await()
                }

            } catch (e: Exception) {
                println("DEBUG: Error deleting message: ${e.message}")
            }
        }
    }

    suspend fun checkUserExists(userId: String): Boolean {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            userDoc.exists()
        } catch (e: Exception) {
            println("DEBUG: Error checking user existence: ${e.message}")
            false
        }
    }

    fun markChatAsRead(chatId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val timestamp = System.currentTimeMillis()
                
                // Update the last read timestamp for the current user
                firestore.collection("chats")
                    .document(chatId)
                    .update("lastRead_$currentUserId", timestamp)
                    .await()

                // Mark all messages as read
                val unreadMessages = firestore.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .whereEqualTo("isRead", false)
                    .whereNotEqualTo("senderId", currentUserId)
                    .get()
                    .await()

                for (message in unreadMessages.documents) {
                    message.reference.update("isRead", true)
                }
            } catch (e: Exception) {
                println("DEBUG: Error marking chat as read: ${e.message}")
            }
        }
    }
} 