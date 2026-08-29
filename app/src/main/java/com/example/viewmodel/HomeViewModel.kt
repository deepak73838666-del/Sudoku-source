package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DailyStreakEntity
import com.example.data.models.Difficulty
import com.example.data.models.GameState
import com.example.data.repository.GameRepository
import com.example.data.repository.SettingsRepository
import com.example.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val gameRepository: GameRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val streak: StateFlow<DailyStreakEntity?> = gameRepository.getStreak()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeGame: StateFlow<GameState?> = gameRepository.getActiveGame()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        
    private val _todayDailyStatus = MutableStateFlow<DailyStatus>(DailyStatus.NotStarted)
    val todayDailyStatus: StateFlow<DailyStatus> = _todayDailyStatus
    
    val onboardingCompleted: StateFlow<Boolean> = settingsRepository.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        viewModelScope.launch {
            gameRepository.getAllHistory().collect { history ->
                val today = DateUtils.getTodayString()
                val isCompletedToday = history.any { it.isDaily && it.seedDate == today }
                
                if (isCompletedToday) {
                    _todayDailyStatus.value = DailyStatus.Completed
                } else {
                    val active = gameRepository.getActiveGameSync()
                    if (active != null && active.isDaily && active.seedDate == today) {
                        _todayDailyStatus.value = DailyStatus.InProgress
                    } else {
                        _todayDailyStatus.value = DailyStatus.NotStarted
                    }
                }
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(true)
        }
    }

    enum class DailyStatus {
        NotStarted, InProgress, Completed
    }
}
