package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Difficulty
import com.example.data.models.GameState
import com.example.data.models.SudokuCell
import com.example.viewmodel.PlayViewModel
import com.example.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreen(
    viewModel: PlayViewModel,
    settingsViewModel: SettingsViewModel,
    adsManager: com.example.ads.FakeAdsManager,
    isDaily: Boolean,
    difficulty: Difficulty,
    isResume: Boolean = false,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(isDaily, difficulty, isResume) {
        val seedDate = if (isDaily) com.example.utils.DateUtils.getTodayString() else null
        viewModel.loadOrGenerateGame(difficulty, isDaily, seedDate, isResume)
    }

    val state by viewModel.gameState.collectAsState()
    
    if (state == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }
    
    val safeState = state!!
    val isPaused by viewModel.isPaused.collectAsState()
    val selectedIndex by viewModel.selectedCellIndex.collectAsState()
    
    val highlightSame by settingsViewModel.highlightSame.collectAsState()
    val mistakesOn by settingsViewModel.mistakesOn.collectAsState()
    val showTimer by settingsViewModel.showTimer.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(if (safeState.isDaily) "Daily Puzzle" else "Practice", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            if (safeState.isDaily) com.example.utils.DateUtils.getDisplayDateString() else safeState.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }, 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (showTimer) {
                        Text(
                            com.example.utils.DateUtils.formatTime(safeState.timerSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.togglePause() }) {
                        Icon(if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause, contentDescription = "Pause/Resume")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (safeState.isCompleted) {
                CompletionView(safeState, onNavigateBack)
            } else if (isPaused) {
                PauseView { viewModel.togglePause() }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mistakes: ${safeState.mistakes}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        // Could add difficulty indicator or score here
                    }
                    
                    SudokuBoardView(
                        cells = safeState.cells,
                        selectedIndex = selectedIndex,
                        highlightSame = highlightSame,
                        mistakesOn = mistakesOn,
                        onCellSelected = { viewModel.selectCell(it) }
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { viewModel.undo() }, modifier = Modifier.size(64.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Filled.Undo, contentDescription = "Undo")
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Undo", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        IconButton(onClick = { viewModel.eraseCell() }, modifier = Modifier.size(64.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Filled.Clear, contentDescription = "Erase")
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Erase", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        IconButton(onClick = { viewModel.useHint() }, modifier = Modifier.size(64.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Filled.Lightbulb, contentDescription = "Hint")
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Hint", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    
                    NumberPad(onNumberClick = { viewModel.inputNumber(it) })
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun PauseView(onResume: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Game Paused", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onResume, 
            modifier = Modifier.fillMaxWidth(0.6f).height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("RESUME", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CompletionView(state: GameState, onHome: () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = scaleIn(initialScale = 0.8f, animationSpec = tween(500)) + fadeIn(animationSpec = tween(500))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Excellent!", 
                style = MaterialTheme.typography.displaySmall, 
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Puzzle Complete", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(32.dp))
            
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(), 
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Time", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(com.example.utils.DateUtils.formatTime(state.timerSeconds), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Mistakes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${state.mistakes}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hints", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${state.hintsUsed}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onHome, 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("CONTINUE", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SudokuBoardView(
    cells: List<SudokuCell>,
    selectedIndex: Int?,
    highlightSame: Boolean,
    mistakesOn: Boolean,
    onCellSelected: (Int) -> Unit
) {
    val selectedCell = selectedIndex?.let { cells[it] }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .border(2.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            .padding(2.5.dp) // padding to make room for border
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0 until 9) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    for (col in 0 until 9) {
                        val index = row * 9 + col
                        val cell = cells[index]
                        
                        val isSelected = index == selectedIndex
                        val isRelated = selectedCell != null && !isSelected && 
                             (cell.row == selectedCell.row || cell.col == selectedCell.col || 
                              (cell.row/3 == selectedCell.row/3 && cell.col/3 == selectedCell.col/3))
                        val isSameValue = highlightSame && selectedCell?.value != 0 && selectedCell?.value == cell.value
                        
                        val bgColor = when {
                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            isSameValue -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            isRelated -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(bgColor)
                                .border(
                                    0.5.dp, 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                                .clickable { onCellSelected(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (cell.value != 0) {
                                val color = when {
                                    mistakesOn && cell.isError -> MaterialTheme.colorScheme.error
                                    cell.isGiven -> MaterialTheme.colorScheme.onSurface
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                Text(
                                    text = cell.value.toString(),
                                    fontSize = 28.sp,
                                    fontWeight = if (cell.isGiven) FontWeight.ExtraBold else FontWeight.Normal,
                                    color = color
                                )
                            } else if (cell.notes.isNotEmpty()) {
                                NotesGrid(cell.notes)
                            }
                        }
                    }
                }
            }
        }
        
        // Draw 3x3 thicker borders internally
        Row(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxHeight().width(1.5.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)))
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxHeight().width(1.5.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)))
            Spacer(modifier = Modifier.weight(1f))
        }
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxWidth().height(1.5.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)))
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxWidth().height(1.5.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)))
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun NotesGrid(notes: Set<Int>) {
    Column(
        modifier = Modifier.fillMaxSize().padding(1.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        for (r in 0 until 3) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                for (c in 0 until 3) {
                    val num = r * 3 + c + 1
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        if (notes.contains(num)) {
                            Text(
                                text = num.toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPad(onNumberClick: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (row in 0 until 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val start = row * 5 + 1
                val end = minOf(start + 4, 9)
                for (i in start..end) {
                    FilledTonalButton(
                        onClick = { onNumberClick(i) },
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(i.toString(), fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (end < start + 4) {
                    Spacer(modifier = Modifier.weight((start + 4 - end).toFloat()))
                }
            }
        }
    }
}
