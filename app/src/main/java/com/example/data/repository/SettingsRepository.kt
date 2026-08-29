package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SOUND_ON = booleanPreferencesKey("sound_on")
        val HAPTICS_ON = booleanPreferencesKey("haptics_on")
        val MISTAKES_ON = booleanPreferencesKey("mistakes_on")
        val SHOW_TIMER = booleanPreferencesKey("show_timer")
        val HIGHLIGHT_SAME = booleanPreferencesKey("highlight_same")
        val DEFAULT_DIFFICULTY = stringPreferencesKey("default_difficulty")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val themeMode: Flow<String> = dataStore.data.map { it[THEME_MODE] ?: "Light" }
    val soundOn: Flow<Boolean> = dataStore.data.map { it[SOUND_ON] ?: true }
    val hapticsOn: Flow<Boolean> = dataStore.data.map { it[HAPTICS_ON] ?: true }
    val mistakesOn: Flow<Boolean> = dataStore.data.map { it[MISTAKES_ON] ?: true }
    val showTimer: Flow<Boolean> = dataStore.data.map { it[SHOW_TIMER] ?: true }
    val highlightSame: Flow<Boolean> = dataStore.data.map { it[HIGHLIGHT_SAME] ?: true }
    val defaultDifficulty: Flow<String> = dataStore.data.map { it[DEFAULT_DIFFICULTY] ?: "MEDIUM" }
    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { it[ONBOARDING_COMPLETED] ?: false }

    suspend fun setThemeMode(mode: String) { dataStore.edit { it[THEME_MODE] = mode } }
    suspend fun setSoundOn(on: Boolean) { dataStore.edit { it[SOUND_ON] = on } }
    suspend fun setHapticsOn(on: Boolean) { dataStore.edit { it[HAPTICS_ON] = on } }
    suspend fun setMistakesOn(on: Boolean) { dataStore.edit { it[MISTAKES_ON] = on } }
    suspend fun setShowTimer(on: Boolean) { dataStore.edit { it[SHOW_TIMER] = on } }
    suspend fun setHighlightSame(on: Boolean) { dataStore.edit { it[HIGHLIGHT_SAME] = on } }
    suspend fun setDefaultDifficulty(difficulty: String) { dataStore.edit { it[DEFAULT_DIFFICULTY] = difficulty } }
    suspend fun setOnboardingCompleted(completed: Boolean) { dataStore.edit { it[ONBOARDING_COMPLETED] = completed } }
}
