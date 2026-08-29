package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.viewmodel.HomeViewModel

@Composable
fun OnboardingScreen(
    viewModel: HomeViewModel,
    onComplete: () -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }
    
    val pages = listOf(
        Pair("Your Daily Sudoku", "One new puzzle every day."),
        Pair("Build Your Streak", "Complete today's puzzle and keep your streak alive."),
        Pair("Improve Your Skills", "Track your times, mistakes and progress.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = pages[currentPage].first,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = pages[currentPage].second,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = {
                if (currentPage < pages.size - 1) {
                    currentPage++
                } else {
                    viewModel.completeOnboarding()
                    onComplete()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (currentPage < pages.size - 1) "NEXT" else "START PLAYING")
        }
    }
}
