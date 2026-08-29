package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DailyStreakEntity
import com.example.data.local.GameHistoryEntity
import com.example.data.models.Difficulty
import com.example.data.repository.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StatisticsViewModel(
    private val repository: GameRepository
) : ViewModel() {

    val history: StateFlow<List<GameHistoryEntity>> = repository.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val streak: StateFlow<DailyStreakEntity?> = repository.getStreak()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val statsSummary: StateFlow<StatsSummary> = history.map { list ->
        val played = list.size
        val completed = list.size // Currently we only save completed games
        val totalTime = list.sumOf { it.timeTakenSeconds }
        val fastest = list.minOfOrNull { it.timeTakenSeconds } ?: 0L
        val average = if (completed > 0) totalTime / completed else 0L
        
        StatsSummary(
            gamesPlayed = played,
            gamesCompleted = completed,
            completionRate = if (played > 0) (completed.toFloat() / played * 100).toInt() else 0,
            averageTime = average,
            fastestTime = fastest,
            totalHints = list.sumOf { it.hintsUsed },
            totalMistakes = list.sumOf { it.mistakes }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsSummary())

    data class StatsSummary(
        val gamesPlayed: Int = 0,
        val gamesCompleted: Int = 0,
        val completionRate: Int = 0,
        val averageTime: Long = 0,
        val fastestTime: Long = 0,
        val totalHints: Int = 0,
        val totalMistakes: Int = 0
    )
}
