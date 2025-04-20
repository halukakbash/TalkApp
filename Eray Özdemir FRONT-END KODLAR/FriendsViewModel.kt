package com.halukakbash.talk_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.halukakbash.talk_app.data.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FriendsViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun getFavoriteUsers(onComplete: (List<User>) -> Unit) {
        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch
                val userDoc = firestore.collection("users")
                    .document(currentUserId)
                    .get()
                    .await()

                val favorites = userDoc.get("favorites") as? List<String> ?: emptyList()
                
                val users = mutableListOf<User>()
                for (userId in favorites) {
                    val favoriteUserDoc = firestore.collection("users")
                        .document(userId)
                        .get()
                        .await()
                    
                    favoriteUserDoc.toObject(User::class.java)?.let { user ->
                        users.add(user)
                    }
                }
                
                onComplete(users)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(emptyList())
            }
        }
    }
} 