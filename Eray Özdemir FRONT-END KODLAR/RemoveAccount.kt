package com.halukakbash.talk_app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import com.halukakbash.talk_app.data.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun updateProfile(
        age: Int,
        country: String,
        gender: String,
        languageLevel: String,
        photoUri: Uri?,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
                var profilePhotoUrl = ""

                // Upload photo if provided
                if (photoUri != null) {
                    val photoRef = storage.reference.child("profile_photos/$userId")
                    photoRef.putFile(photoUri).await()
                    profilePhotoUrl = photoRef.downloadUrl.await().toString()
                }

                // Update user profile in Firestore
                val userUpdates = hashMapOf(
                    "age" to age,
                    "country" to country,
                    "gender" to gender,
                    "languageLevel" to languageLevel,
                    "profilePhotoUrl" to profilePhotoUrl
                )

                firestore.collection("users")
                    .document(userId)
                    .update(userUpdates as Map<String, Any>)
                    .await()

                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun getCurrentUser(onComplete: (User?) -> Unit) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val userDoc = firestore.collection("users")
                        .document(userId)
                        .get()
                        .await()
                    
                    val user = userDoc.toObject(User::class.java)
                    onComplete(user)
                } else {
                    onComplete(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(null)
            }
        }
    }

    fun signOut() {
        auth.signOut()
        // You might want to navigate back to login screen here
    }

    fun deleteAccount(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: return@launch
                
                firestore.collection("users")
                    .document(user.uid)
                    .delete()
                    .await()
                    
                user.delete().await()
                auth.signOut()
                
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun checkUserExists(userId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val userDoc = firestore.collection("users").document(userId).get().await()
                onComplete(userDoc.exists())
            } catch (e: Exception) {
                println("DEBUG: Error checking user existence: ${e.message}")
                onComplete(false)
            }
        }
    }

    fun getUser(userId: String, onComplete: (User?) -> Unit) {
        viewModelScope.launch {
            try {
                val userDoc = firestore.collection("users").document(userId).get().await()
                val user = userDoc.toObject(User::class.java)
                onComplete(user)
            } catch (e: Exception) {
                println("DEBUG: Error getting user: ${e.message}")
                onComplete(null)
            }
        }
    }

    fun addToFavorites(userId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(currentUserId)
            .update("favorites", FieldValue.arrayUnion(userId))
    }

    fun removeFromFavorites(userId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(currentUserId)
            .update("favorites", FieldValue.arrayRemove(userId))
    }

    fun isUserFavorite(userId: String, callback: (Boolean) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val favorites = document.get("favorites") as? List<String> ?: emptyList()
                callback(favorites.contains(userId))
            }
    }
} 
