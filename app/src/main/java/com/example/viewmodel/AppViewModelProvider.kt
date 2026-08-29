package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.data.repository.GameRepository
import com.example.data.repository.SettingsRepository

class AppViewModelProvider(
    private val gameRepository: GameRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(gameRepository, settingsRepository) as T
            }
            modelClass.isAssignableFrom(PlayViewModel::class.java) -> {
                PlayViewModel(gameRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(settingsRepository) as T
            }
            modelClass.isAssignableFrom(StatisticsViewModel::class.java) -> {
                StatisticsViewModel(gameRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: \${modelClass.name}")
        }
    }
}
