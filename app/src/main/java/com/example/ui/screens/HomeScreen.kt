package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.models.Difficulty
import com.example.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onPlayToday: () -> Unit,
    onPractice: (Difficulty) -> Unit,
    onResume: () -> Unit
) {
    val streak by viewModel.streak.collectAsState()
    val dailyStatus by viewModel.todayDailyStatus.collectAsState()
    val activeGame by viewModel.activeGame.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sudoku Daily", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                StreakCard(streak?.currentStreak ?: 0, streak?.bestStreak ?: 0)
            }
            
            item {
                DailyPuzzleCard(dailyStatus, activeGame?.isDaily == true, onPlayToday)
            }
            
            item {
                Text(
                    "Quick Practice",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                if (activeGame != null && !activeGame!!.isDaily && !activeGame!!.isCompleted) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Resume Practice", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text("Difficulty: ${activeGame!!.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha=0.8f))
                            }
                            FilledTonalButton(onClick = { onResume() }) {
                                Text("CONTINUE")
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PracticeButton(Difficulty.EASY, Modifier.weight(1f), onPractice)
                    PracticeButton(Difficulty.MEDIUM, Modifier.weight(1f), onPractice)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PracticeButton(Difficulty.HARD, Modifier.weight(1f), onPractice)
                    PracticeButton(Difficulty.EXPERT, Modifier.weight(1f), onPractice)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StreakCard(current: Int, best: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("$current Day Streak", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Keep it up!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.8f))
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    Icons.Outlined.EmojiEvents,
                    contentDescription = "Best",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
                Text("Best: $best", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun DailyPuzzleCard(
    status: HomeViewModel.DailyStatus,
    isActiveDaily: Boolean,
    onPlay: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("TODAY'S PUZZLE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, letterSpacing = androidx.compose.ui.unit.TextUnit(1.5f, androidx.compose.ui.unit.TextUnitType.Sp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(com.example.utils.DateUtils.getDisplayDateString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(24.dp))
            
            when (status) {
                HomeViewModel.DailyStatus.NotStarted -> {
                    Text("Your daily puzzle is waiting.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onPlay, 
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("PLAY TODAY'S PUZZLE", fontWeight = FontWeight.Bold)
                    }
                }
                HomeViewModel.DailyStatus.InProgress -> {
                    Text("Keep your streak alive!", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onPlay, 
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("CONTINUE PUZZLE", fontWeight = FontWeight.Bold)
                    }
                }
                HomeViewModel.DailyStatus.Completed -> {
                    Text("Great job! Come back tomorrow.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = { /* Already completed */ }, 
                        modifier = Modifier.fillMaxWidth().height(56.dp), 
                        enabled = false,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("COMPLETED ✓", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PracticeButton(difficulty: Difficulty, modifier: Modifier = Modifier, onClick: (Difficulty) -> Unit) {
    FilledTonalButton(
        onClick = { onClick(difficulty) },
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(difficulty.name.lowercase().replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.SemiBold)
    }
}
