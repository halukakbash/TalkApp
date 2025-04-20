package com.halukakbash.talk_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.halukakbash.talk_app.navigation.NavigationItem
import com.halukakbash.talk_app.screens.HomeScreen
import com.halukakbash.talk_app.screens.ChatsScreen
import com.halukakbash.talk_app.screens.AiChatScreen
import com.halukakbash.talk_app.screens.ProfileScreen
import com.halukakbash.talk_app.screens.LoginScreen
import com.halukakbash.talk_app.screens.SignUpScreen
import com.halukakbash.talk_app.screens.ProfileSetupScreen
import com.halukakbash.talk_app.ui.theme.Talk_appTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import com.halukakbash.talk_app.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.halukakbash.talk_app.viewmodel.HomeViewModel
import androidx.compose.runtime.collectAsState
import com.halukakbash.talk_app.screens.UserProfileScreen
import com.halukakbash.talk_app.screens.ChatScreen
import com.halukakbash.talk_app.screens.QuizScreen
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.unit.dp
import com.halukakbash.talk_app.screens.EditProfileScreen
import com.halukakbash.talk_app.viewmodel.ProfileViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.halukakbash.talk_app.data.User
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import com.halukakbash.talk_app.screens.FriendsScreen
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        updateOnlineStatus(true)
        setContent {
            Talk_appTheme {
                MainScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        updateOnlineStatus(false)
    }

    private fun updateOnlineStatus(isOnline: Boolean) {
        auth.currentUser?.let { user ->
            firestore.collection("users")
                .document(user.uid)
                .update("isOnline", isOnline)
        }
    }
}

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val startDestination = if (authViewModel.isUserLoggedIn) {
        NavigationItem.Home.route
    } else {
        NavigationItem.Login.route
    }
    
    Scaffold(
        bottomBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            val routesWithBottomNav = listOf(
                NavigationItem.Home.route,
                NavigationItem.Chats.route,
                NavigationItem.AiChat.route,
                NavigationItem.Vocabulary.route,
                NavigationItem.Friends.route,
                NavigationItem.Profile.route
            )
            
            if (currentRoute in routesWithBottomNav) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(route = NavigationItem.Login.route) {
                LoginScreen(
                    onNavigateToSignUp = { navController.navigate(NavigationItem.SignUp.route) },
                    onLoginSuccess = { navController.navigate(NavigationItem.Home.route) {
                        popUpTo(NavigationItem.Login.route) { inclusive = true }
                    }}
                )
            }
            composable(route = NavigationItem.SignUp.route) {
                SignUpScreen(
                    onNavigateToLogin = { navController.navigate(NavigationItem.Login.route) },
                    onSignUpSuccess = { 
                        navController.navigate(NavigationItem.Home.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(route = NavigationItem.Home.route) { 
                HomeScreen(navController = navController) 
            }
            composable(route = NavigationItem.Chats.route) { 
                ChatsScreen(navController = navController) 
            }
            composable(route = NavigationItem.AiChat.route) { 
                AiChatScreen(navController = navController) 
            }
            composable(route = NavigationItem.Vocabulary.route) { 
                QuizScreen(navController = navController) 
            }
            composable(route = NavigationItem.Profile.route) { 
                ProfileScreen(navController = navController) 
            }
            composable(route = NavigationItem.Friends.route) {
                FriendsScreen(navController = navController)
            }
            composable(
                route = NavigationItem.UserProfile.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                userId?.let {
                    UserProfileScreen(
                        userId = it,
                        navController = navController
                    )
                }
            }
            composable(
                route = NavigationItem.Chat.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                ChatScreen(
                    chatId = "", // This will be generated in the ChatViewModel
                    partnerId = userId,
                    navController = navController
                )
            }
            composable(route = "edit_profile") {
                val profileViewModel: ProfileViewModel = viewModel()
                val userState = remember { mutableStateOf<User?>(null) }
                
                LaunchedEffect(Unit) {
                    profileViewModel.getCurrentUser { fetchedUser ->
                        userState.value = fetchedUser
                    }
                }
                
                userState.value?.let { currentUser ->
                    EditProfileScreen(
                        navController = navController,
                        user = currentUser
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Chats,
        NavigationItem.AiChat,
        NavigationItem.Vocabulary,
        NavigationItem.Friends,
        NavigationItem.Profile
    )
    
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary,
        tonalElevation = 0.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        
        items.forEach { item ->
            NavigationBarItem(
                icon = { 
                    Icon(
                        painter = painterResource(id = item.icon), 
                        contentDescription = item.title,
                        modifier = Modifier.size(22.dp)
                    ) 
                },
                label = { 
                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        maxLines = 1
                    ) 
                },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                )
            )
        }
    }
}