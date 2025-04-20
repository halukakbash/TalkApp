package com.halukakbash.talk_app.navigation

import com.halukakbash.talk_app.R

sealed class NavigationItem(val route: String, val icon: Int, val title: String) {
    object Home : NavigationItem("home", R.drawable.ic_home, "Home")
    object Chats : NavigationItem("chats", R.drawable.ic_chat, "Chats")
    object AiChat : NavigationItem("ai_chat", R.drawable.ic_ai, "AI Chat")
    object Vocabulary : NavigationItem("vocabulary", R.drawable.ic_quiz, "Quiz")
    object Friends : NavigationItem("friends", R.drawable.ic_contacts, "Contacts")
    object Profile : NavigationItem("profile", R.drawable.ic_profile, "Profile")
    object Login : NavigationItem("login", 0, "Login")
    object SignUp : NavigationItem("signup", 0, "Sign Up")
    object UserProfile : NavigationItem("user_profile/{userId}", 0, "User Profile")
    object Chat : NavigationItem("chat/{userId}", 0, "Chat")
    object EditProfile : NavigationItem("edit_profile", R.drawable.ic_profile, "Edit Profile")
} 