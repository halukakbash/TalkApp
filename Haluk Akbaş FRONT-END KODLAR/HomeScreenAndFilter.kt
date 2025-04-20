package com.halukakbash.talk_app.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.halukakbash.talk_app.viewmodel.HomeViewModel
import coil.compose.AsyncImage
import com.halukakbash.talk_app.data.User
import com.halukakbash.talk_app.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    var showFilterDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    val users by viewModel.users.collectAsState()
    val selectedLevel = remember { mutableStateOf<String?>(null) }
    var isInitialLoad by remember { mutableStateOf(true) }

    // Set isInitialLoad to false after first user load
    LaunchedEffect(users) {
        if (users.isNotEmpty()) {
            isInitialLoad = false
        }
    }

    // Rating Dialog
    if (showRatingDialog && selectedUser != null) {
        RatingDialog(
            userName = selectedUser!!.name,
            onDismiss = { showRatingDialog = false },
            onSubmit = { rating, comment ->
                // This would normally send to backend
                showRatingDialog = false
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFilterDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (users.isEmpty() && !isInitialLoad) {
                // Show no users message only after filter is applied
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "No users found with selected filter: ${selectedLevel.value ?: "none"}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { 
                                selectedLevel.value = null
                                viewModel.clearFilter()
                            }
                        ) {
                            Text("Clear Filter")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(users) { user ->
                        UserListItem(
                            user = user,
                            onUserClick = {
                                navController.navigate("user_profile/${user.id}")
                            },
                            onCallClick = {
                                selectedUser = user
                                showRatingDialog = true
                            }
                        )
                    }
                }
            }

            if (showFilterDialog) {
                FilterDialog(
                    onDismiss = { showFilterDialog = false },
                    onFilterSelected = { level ->
                        selectedLevel.value = level
                        viewModel.filterByLanguageLevel(level)
                        showFilterDialog = false
                    },
                    selectedLevel = selectedLevel.value
                )
            }
        }
    }
}

@Composable
private fun FilterDialog(
    onDismiss: () -> Unit,
    onFilterSelected: (String?) -> Unit,
    selectedLevel: String?
) {
    val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
    var selectedLevels by remember { 
        mutableStateOf(
            if (selectedLevel?.contains("-") == true) {
                selectedLevel.split("-").toSet()
            } else setOf()
        )
    }
    
    val barHeights = listOf(40.dp, 30.dp, 60.dp, 60.dp, 70.dp, 20.dp)
    val primaryColor = MaterialTheme.colorScheme.primary
    val inactiveColor = Color.LightGray.copy(alpha = 0.3f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("English level") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                // Level bars with click support
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    levels.forEachIndexed { index, level ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.clickable {
                                selectedLevels = if (selectedLevels.contains(level)) {
                                    selectedLevels - level
                                } else {
                                    selectedLevels + level
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(barHeights[index])
                                    .background(
                                        color = if (level in selectedLevels) primaryColor else inactiveColor,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = level,
                                color = if (level in selectedLevels) primaryColor else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Selected levels display
                if (selectedLevels.isNotEmpty()) {
                    Text(
                        text = "Selected levels: ${selectedLevels.joinToString(", ")}",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Clear Filter Button
                OutlinedButton(
                    onClick = { 
                        onFilterSelected(null)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryColor
                    ),
                    border = BorderStroke(1.dp, primaryColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Clear Filter")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Apply Button
                Button(
                    onClick = { 
                        if (selectedLevels.isNotEmpty()) {
                            onFilterSelected(selectedLevels.joinToString(","))
                        }
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Apply")
                }
            }
        },
        confirmButton = { },
        dismissButton = { }
    )
}

@Composable
fun UserListItem(
    user: User,
    onUserClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onUserClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image with Status Indicator
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clickable(onClick = onUserClick)
            ) {
                AsyncImage(
                    model = user.profilePhotoUrl,
                    contentDescription = "Profile photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_profile)
                )
                
                // Online/Offline Status Indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (user.isOnline) Color(0xFF4CAF50) else Color.Gray)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
            
            // User Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = user.languageLevel,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_thumb_up),
                        contentDescription = "Rating",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${user.rating}%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                    )
                    
                    Text(
                        text = "${user.gender} • ${user.country} • ${user.talks} talks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            IconButton(onClick = onCallClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_call),
                    contentDescription = "Call",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun RatingDialog(
    userName: String,
    onDismiss: () -> Unit,
    onSubmit: (Float, String) -> Unit
) {
    var rating by remember { mutableStateOf(5f) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Rate your conversation with $userName")
        },
        text = {
            Column {
                Text("How was your conversation?")
                
                // Star Rating
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { rating = i.toFloat() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (i <= rating) R.drawable.ic_star_filled 
                                    else R.drawable.ic_star_outline
                                ),
                                contentDescription = "Star $i",
                                tint = if (i <= rating) Color(0xFFFFC107) else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comments (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(rating, comment) }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 
