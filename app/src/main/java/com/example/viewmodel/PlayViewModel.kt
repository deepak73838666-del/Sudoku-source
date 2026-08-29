package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Difficulty
import com.example.data.models.GameState
import com.example.data.models.SudokuCell
import com.example.data.repository.GameRepository
import com.example.domain.sudoku.SudokuEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayViewModel(
    private val repository: GameRepository
) : ViewModel() {

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _selectedCellIndex = MutableStateFlow<Int?>(null)
    val selectedCellIndex: StateFlow<Int?> = _selectedCellIndex.asStateFlow()

    private val _isNotesMode = MutableStateFlow(false)
    val isNotesMode: StateFlow<Boolean> = _isNotesMode.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val history = mutableListOf<List<SudokuCell>>() // For undo
    private var timerJob: Job? = null

    fun loadOrGenerateGame(difficulty: Difficulty, isDaily: Boolean, seedDate: String?, isResume: Boolean = false) {
        viewModelScope.launch {
            val active = repository.getActiveGameSync()
            if (active != null) {
                // If it's a resume request from HomeScreen, resume the active game
                if (isResume) {
                    resumeGame(active)
                    return@launch
                }
                // Determine if we should resume or start fresh daily
                if (isDaily) {
                    if (active.isDaily && active.seedDate == seedDate) {
                        resumeGame(active)
                        return@launch
                    }
                }
            }
            // Generate new
            generateNewGame(difficulty, isDaily, seedDate)
        }
    }

    private fun resumeGame(state: GameState) {
        _gameState.value = state
        history.clear()
        history.add(state.cells)
        if (!state.isCompleted) {
            startTimer()
        }
    }

    private suspend fun generateNewGame(difficulty: Difficulty, isDaily: Boolean, seedDate: String?) {
        withContext(Dispatchers.Default) {
            val seed = if (isDaily && seedDate != null) com.example.utils.DateUtils.getSeedFromDate(seedDate) else null
            val (puzzle, solution) = SudokuEngine.generatePuzzle(difficulty.targetClues, seed)
            
            val cells = puzzle.mapIndexed { index, value ->
                SudokuCell(
                    index = index,
                    value = value,
                    isGiven = value != 0
                )
            }
            
            val newState = GameState(
                cells = cells,
                solution = solution,
                difficulty = difficulty,
                isDaily = isDaily,
                seedDate = seedDate
            )
            
            withContext(Dispatchers.Main) {
                _gameState.value = newState
                history.clear()
                history.add(cells)
                repository.saveActiveGame(newState)
                startTimer()
            }
        }
    }

    fun selectCell(index: Int) {
        if (_gameState.value?.isCompleted == true || _isPaused.value) return
        _selectedCellIndex.value = index
    }

    fun toggleNotesMode() {
        _isNotesMode.value = !_isNotesMode.value
    }

    fun inputNumber(num: Int, autoRemove: Boolean = true) {
        val state = _gameState.value ?: return
        if (state.isCompleted || _isPaused.value) return
        val selected = _selectedCellIndex.value ?: return

        val cell = state.cells[selected]
        if (cell.isGiven) return

        val newCells = state.cells.toMutableList()

        if (_isNotesMode.value) {
            // Toggle note
            val currentNotes = cell.notes.toMutableSet()
            if (currentNotes.contains(num)) {
                currentNotes.remove(num)
            } else {
                currentNotes.add(num)
            }
            newCells[selected] = cell.copy(notes = currentNotes, value = 0, isError = false)
        } else {
            // Set value
            if (cell.value == num) return // same value
            val isError = state.solution[selected] != num
            newCells[selected] = cell.copy(value = num, notes = emptySet(), isError = isError)

            // Auto remove notes from related cells
            if (!isError && autoRemove) {
                removeNotesFromRelated(newCells, selected, num)
            }
        }

        updateGameState(newCells)
    }

    fun eraseCell() {
        val state = _gameState.value ?: return
        if (state.isCompleted || _isPaused.value) return
        val selected = _selectedCellIndex.value ?: return

        val cell = state.cells[selected]
        if (cell.isGiven || (cell.value == 0 && cell.notes.isEmpty())) return

        val newCells = state.cells.toMutableList()
        newCells[selected] = cell.copy(value = 0, notes = emptySet(), isError = false)

        updateGameState(newCells)
    }

    fun undo() {
        if (history.size > 1) {
            history.removeLast()
            val previousCells = history.last()
            _gameState.update { it?.copy(cells = previousCells) }
            saveCurrentState()
        }
    }

    fun useHint(autoRemove: Boolean = true) {
        val state = _gameState.value ?: return
        if (state.isCompleted || _isPaused.value) return
        val selected = _selectedCellIndex.value ?: return

        val cell = state.cells[selected]
        if (cell.isGiven || cell.value == state.solution[selected]) return

        val correctValue = state.solution[selected]
        val newCells = state.cells.toMutableList()
        newCells[selected] = cell.copy(value = correctValue, notes = emptySet(), isError = false)
        
        if (autoRemove) {
            removeNotesFromRelated(newCells, selected, correctValue)
        }

        _gameState.update { 
            it?.copy(cells = newCells, hintsUsed = it.hintsUsed + 1)
        }
        history.add(newCells)
        saveCurrentState()
        checkCompletion()
    }

    private fun removeNotesFromRelated(cells: MutableList<SudokuCell>, index: Int, num: Int) {
        val row = index / 9
        val col = index % 9
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3

        for (i in 0 until 81) {
            val r = i / 9
            val c = i % 9
            if (r == row || c == col || (r >= boxRow && r < boxRow + 3 && c >= boxCol && c < boxCol + 3)) {
                if (i != index && cells[i].notes.contains(num)) {
                    val newNotes = cells[i].notes - num
                    cells[i] = cells[i].copy(notes = newNotes)
                }
            }
        }
    }

    private fun updateGameState(newCells: List<SudokuCell>) {
        val oldState = _gameState.value ?: return
        
        // Count new mistakes
        var newMistakesCount = oldState.mistakes
        val selected = _selectedCellIndex.value
        if (selected != null && !_isNotesMode.value) {
            val newVal = newCells[selected].value
            if (newVal != 0 && newVal != oldState.solution[selected]) {
                newMistakesCount++
            }
        }

        _gameState.update {
            it?.copy(cells = newCells, mistakes = newMistakesCount)
        }
        
        history.add(newCells)
        saveCurrentState()
        checkCompletion()
    }

    private fun checkCompletion() {
        val state = _gameState.value ?: return
        var complete = true
        for (i in 0 until 81) {
            if (state.cells[i].value != state.solution[i]) {
                complete = false
                break
            }
        }

        if (complete) {
            stopTimer()
            _gameState.update { it?.copy(isCompleted = true, cells = it.cells.map { c -> c.copy(isError = false) }) }
            saveCurrentState() // save completed state
            
            // Save to history
            viewModelScope.launch {
                val finalState = _gameState.value!!
                val historyEntity = com.example.data.local.GameHistoryEntity(
                    dateCompleted = System.currentTimeMillis(),
                    timeTakenSeconds = finalState.timerSeconds,
                    difficulty = finalState.difficulty.name,
                    mistakes = finalState.mistakes,
                    hintsUsed = finalState.hintsUsed,
                    isDaily = finalState.isDaily,
                    seedDate = finalState.seedDate
                )
                repository.saveCompletedGame(historyEntity)
                
                if (finalState.isDaily) {
                    updateStreak()
                }
                repository.clearActiveGame()
            }
        }
    }

    private suspend fun updateStreak() {
        val currentStreakEntity = repository.getStreak().firstOrNull()
        
        val today = com.example.utils.DateUtils.getTodayString()
        val yesterday = com.example.utils.DateUtils.getYesterdayString()
        
        val current = currentStreakEntity?.currentStreak ?: 0
        val best = currentStreakEntity?.bestStreak ?: 0
        val lastDate = currentStreakEntity?.lastCompletedDateString
        
        val newStreak = if (lastDate == yesterday) {
            current + 1
        } else if (lastDate == today) {
            current // Already completed today (though logic shouldn't allow re-playing daily, just in case)
        } else {
            1
        }
        
        val newBest = maxOf(best, newStreak)
        
        repository.updateStreak(
            com.example.data.local.DailyStreakEntity(
                currentStreak = newStreak,
                bestStreak = newBest,
                lastCompletedDateString = today
            )
        )
    }

    private fun saveCurrentState() {
        _gameState.value?.let { state ->
            viewModelScope.launch {
                repository.saveActiveGame(state)
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_isPaused.value && _gameState.value?.isCompleted == false) {
                    _gameState.update { it?.copy(timerSeconds = it.timerSeconds + 1) }
                    // Save occasionally, every 10 seconds to avoid too much DB writing
                    if ((_gameState.value?.timerSeconds ?: 0) % 10 == 0L) {
                        saveCurrentState()
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    fun togglePause() {
        _isPaused.value = !_isPaused.value
        if (_isPaused.value) {
            saveCurrentState()
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveCurrentState()
        stopTimer()
    }
}
