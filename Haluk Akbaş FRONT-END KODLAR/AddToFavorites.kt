package com.halukakbash.talk_app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.halukakbash.talk_app.data.User
import com.halukakbash.talk_app.viewmodel.ProfileViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    var userExists by remember { mutableStateOf(true) }
    var user by remember { mutableStateOf<User?>(null) }
    var isFavorite by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    
    LaunchedEffect(userId) {
        viewModel.checkUserExists(userId) { exists ->
            if (!exists) {
                userExists = false
                navController.navigateUp()
            }
        }
        
        viewModel.getUser(userId) { loadedUser ->
            user = loadedUser
        }
    }

    LaunchedEffect(userId) {
        viewModel.isUserFavorite(userId) { isFav ->
            isFavorite = isFav
        }
    }

    if (!userExists) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                "User no longer exists",
                modifier = Modifier.align(Alignment.Center)
            )
        }
        return
    }

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        return
    }

    // Now that we've checked user is not null, create a local val to use within the composable
    val currentUser = user!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            isFavorite = !isFavorite
                            if (isFavorite) {
                                viewModel.addToFavorites(userId)
                                showSnackbar = true
                            } else {
                                viewModel.removeFromFavorites(userId)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                AsyncImage(
                    model = currentUser.profilePhotoUrl,
                    contentDescription = "Profile photo",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${currentUser.name} ${currentUser.lastName}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = { 
                        navController.navigate("chat/${currentUser.id}")
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Icon(
                        Icons.Default.Message,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Message")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                InfoRow(
                    icon = { Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    title = "Native language",
                    value = currentUser.nativeLanguage.ifEmpty { "Not specified" }
                )

                InfoRow(
                    icon = { Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    title = "English level",
                    value = currentUser.languageLevel
                )

                InfoRow(
                    icon = { Icon(if (currentUser.gender == "Male") Icons.Default.Male else Icons.Default.Female, 
                           contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    title = "Gender",
                    value = currentUser.gender
                )

                InfoRow(
                    icon = { Icon(Icons.Default.Cake, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    title = "Age",
                    value = "${currentUser.age} years old"
                )

                InfoRow(
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    title = "Country",
                    value = currentUser.country.ifEmpty { "Not specified" }
                )

                InfoRow(
                    icon = { Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    title = "Rating",
                    value = "${currentUser.rating}%"
                )

                InfoRow(
                    icon = { Icon(Icons.Default.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    title = "Total Talks",
                    value = "${currentUser.talks} talks"
                )
            }
        }

        if (showSnackbar) {
            LaunchedEffect(Unit) {
                delay(3000)
                showSnackbar = false
            }
            
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {}
            ) {
                Text("User has been added to favorite friends")
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: @Composable () -> Unit,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
        }
        
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
} 
