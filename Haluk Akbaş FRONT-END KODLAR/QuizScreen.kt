package com.halukakbash.talk_app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import androidx.lifecycle.viewmodel.compose.viewModel
import com.halukakbash.talk_app.viewmodel.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    navController: NavController
) {
    val vocabularyCategories = listOf(
        VocabularyCategory(
            title = "Common Words",
            icon = Icons.Default.Star,
            gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF45A049)),
            description = "Essential everyday vocabulary"
        ),
        VocabularyCategory(
            title = "Business",
            icon = Icons.Default.Business,
            gradientColors = listOf(Color(0xFF2196F3), Color(0xFF1976D2)),
            description = "Professional and workplace terms"
        ),
        VocabularyCategory(
            title = "Travel",
            icon = Icons.Default.FlightTakeoff,
            gradientColors = listOf(Color(0xFFFF9800), Color(0xFFF57C00)),
            description = "Travel and tourism vocabulary"
        ),
        VocabularyCategory(
            title = "Social",
            icon = Icons.Default.People,
            gradientColors = listOf(Color(0xFF9C27B0), Color(0xFF7B1FA2)),
            description = "Social interaction phrases"
        ),
        VocabularyCategory(
            title = "Academic",
            icon = Icons.Default.School,
            gradientColors = listOf(Color(0xFFF44336), Color(0xFFD32F2F)),
            description = "Academic and educational terms"
        ),
        VocabularyCategory(
            title = "Technology",
            icon = Icons.Default.Computer,
            gradientColors = listOf(Color(0xFF607D8B), Color(0xFF455A64)),
            description = "Tech and digital vocabulary"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            "Words",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 18.sp
                            )
                        ) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.height(48.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            // Section Title
            Text(
                text = "Choose a Category",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
            
            // Categories Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(vocabularyCategories) { category ->
                    VocabularyCategoryCard(
                        category = category,
                        onClick = {
                            navController.navigate("vocabulary/${category.title.lowercase()}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VocabularyCategoryCard(
    category: VocabularyCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = category.gradientColors
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon Section
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Text Section
                Column {
                    Text(
                        text = category.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = category.description,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

data class VocabularyCategory(
    val title: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val description: String
) 