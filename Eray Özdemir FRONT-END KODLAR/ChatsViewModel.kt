package com.halukakbash.talk_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentSnapshot
import com.halukakbash.talk_app.data.ChatPreview
import com.halukakbash.talk_app.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.AggregateSource

class ChatsViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _chats = MutableStateFlow<List<ChatPreview>>(emptyList())
    val chats = _chats.asStateFlow()

    init {
        listenToChats()
    }

    private fun listenToChats() {
        val currentUserId = auth.currentUser?.uid ?: return
        println("DEBUG: Starting to listen to chats for user: $currentUserId")

        firestore.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("DEBUG: Error listening to chats: ${error.message}")
                    return@addSnapshotListener
                }

                viewModelScope.launch {
                    try {
                        val chatPreviews = mutableListOf<ChatPreview>()

                        snapshot?.documents?.forEach { doc ->
                            try {
                                val participants = doc.get("participants") as? List<String>
                                val otherUserId = participants?.find { it != currentUserId }
                                
                                if (otherUserId == null) {
                                    println("DEBUG: Could not find other user for chat ${doc.id}")
                                    return@forEach
                                }

                                val otherUserDoc = firestore.collection("users")
                                    .document(otherUserId)
                                    .get()
                                    .await()

                                val otherUser = otherUserDoc.toObject(User::class.java)

                                if (otherUser == null) {
                                    println("DEBUG: Could not find other user data for ID $otherUserId")
                                    return@forEach
                                }

                                val chatPreview = ChatPreview(
                                    chatId = doc.id,
                                    otherUser = otherUser.copy(id = otherUserId),
                                    lastMessage = doc.getString("lastMessage") ?: "",
                                    lastMessageTime = doc.getLong("lastMessageTime") ?: System.currentTimeMillis(),
                                    lastMessageSenderId = doc.getString("lastMessageSenderId") ?: "",
                                    unreadCount = calculateUnreadCount(doc, currentUserId)
                                )
                                chatPreviews.add(chatPreview)

                            } catch (e: Exception) {
                                println("DEBUG: Error processing chat document ${doc.id}: ${e.message}")
                            }
                        }

                        _chats.value = chatPreviews
                    } catch (e: Exception) {
                        println("DEBUG: Error processing chats snapshot: ${e.message}")
                    }
                }
            }
    }

    private suspend fun calculateUnreadCount(
        chatDoc: DocumentSnapshot,
        currentUserId: String
    ): Int {
        val lastReadTimestamp = chatDoc.getLong("lastRead_$currentUserId") ?: 0L
        return try {
            firestore.collection("chats")
                .document(chatDoc.id)
                .collection("messages")
                .whereGreaterThan("timestamp", lastReadTimestamp)
                .whereEqualTo("isRead", false)
                .whereNotEqualTo("senderId", currentUserId)
                .count()
                .get(AggregateSource.SERVER)
                .await()
                .count.toInt()
        } catch (e: Exception) {
            println("DEBUG: Error calculating unread count: ${e.message}")
            0
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
                e.printStackTrace()
            }
        }
    }

    fun deleteConversation(chatId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch
                
                // First update the participants array to remove current user
                val chatRef = firestore.collection("chats").document(chatId)
                val chatDoc = chatRef.get().await()
                val participants = chatDoc.get("participants") as? List<String> ?: return@launch
                
                // Remove current user from participants
                val updatedParticipants = participants.filter { it != currentUserId }
                
                if (updatedParticipants.isEmpty()) {
                    // If no participants left, delete the entire chat
                    // First delete all messages
                    val messages = chatRef
                        .collection("messages")
                        .get()
                        .await()

                    messages.documents.forEach { doc ->
                        chatRef
                            .collection("messages")
                            .document(doc.id)
                            .delete()
                            .await()
                    }

                    // Then delete the chat document
                    chatRef.delete().await()
                } else {
                    // Otherwise just update participants
                    chatRef.update("participants", updatedParticipants).await()
                }

                // Update UI immediately
                _chats.value = _chats.value.filter { it.chatId != chatId }
                
                println("DEBUG: Successfully deleted/left conversation $chatId")

            } catch (e: Exception) {
                println("DEBUG: Error deleting conversation: ${e.message}")
                e.printStackTrace()
            }
        }
    }
} 