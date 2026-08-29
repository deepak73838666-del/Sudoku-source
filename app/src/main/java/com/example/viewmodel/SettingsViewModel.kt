package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val themeMode = repository.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Light")
    val soundOn = repository.soundOn.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val hapticsOn = repository.hapticsOn.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val mistakesOn = repository.mistakesOn.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val showTimer = repository.showTimer.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val highlightSame = repository.highlightSame.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val defaultDifficulty = repository.defaultDifficulty.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "MEDIUM")

    fun setThemeMode(mode: String) = viewModelScope.launch { repository.setThemeMode(mode) }
    fun setSoundOn(on: Boolean) = viewModelScope.launch { repository.setSoundOn(on) }
    fun setHapticsOn(on: Boolean) = viewModelScope.launch { repository.setHapticsOn(on) }
    fun setMistakesOn(on: Boolean) = viewModelScope.launch { repository.setMistakesOn(on) }
    fun setShowTimer(on: Boolean) = viewModelScope.launch { repository.setShowTimer(on) }
    fun setHighlightSame(on: Boolean) = viewModelScope.launch { repository.setHighlightSame(on) }
    fun setDefaultDifficulty(difficulty: String) = viewModelScope.launch { repository.setDefaultDifficulty(difficulty) }
}
