package com.halukakbash.talk_app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.halukakbash.talk_app.data.User
import com.halukakbash.talk_app.data.LanguageLevel

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    private val _needsProfileSetup = MutableStateFlow(false)
    val needsProfileSetup: StateFlow<Boolean> = _needsProfileSetup

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    fun signUp(
        email: String,
        password: String,
        name: String,
        lastName: String,
        age: Int,
        country: String,
        gender: String,
        languageLevel: String,
        nativeLanguage: String,
        photoUri: Uri?
    ) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                println("DEBUG: Starting signup process...")
                
                // Create authentication user
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                
                result.user?.let { firebaseUser ->
                    var profilePhotoUrl = ""
                    
                    // Upload photo if provided
                    if (photoUri != null) {
                        try {
                            val photoRef = storage.reference
                                .child("profile_photos")
                                .child(firebaseUser.uid)
                                .child("profile.jpg")
                            photoRef.putFile(photoUri).await()
                            profilePhotoUrl = photoRef.downloadUrl.await().toString()
                        } catch (e: Exception) {
                            println("DEBUG: Photo upload failed: ${e.message}")
                            // Continue with empty photo URL if upload fails
                        }
                    }

                    val user = User(
                        id = firebaseUser.uid,
                        name = name,
                        lastName = lastName,
                        email = email,
                        age = age,
                        country = country,
                        gender = gender,
                        languageLevel = languageLevel,
                        nativeLanguage = nativeLanguage,
                        profilePhotoUrl = profilePhotoUrl,
                        isOnline = true,  // Set initial online status
                        rating = 100,     // Initial rating
                        talks = 0         // Initial number of talks
                    )
                    
                    println("DEBUG: Created user object, saving to Firestore...")
                    
                    // Save complete user data to Firestore
                    firestore.collection("users")
                        .document(firebaseUser.uid)
                        .set(user)
                        .await()
                    
                    println("DEBUG: User saved to Firestore, setting success state...")
                    _authState.value = AuthState.Success
                    println("DEBUG: Auth state set to Success")
                } ?: throw Exception("Failed to create user")
                
            } catch (e: Exception) {
                println("DEBUG: Signup error: ${e.message}")
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.signInWithEmailAndPassword(email, password).await()
                
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val userDoc = firestore.collection("users").document(userId).get().await()
                    _needsProfileSetup.value = !userDoc.exists() || 
                        userDoc.getString("languageLevel").isNullOrEmpty()
                }
                
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Initial
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
} 