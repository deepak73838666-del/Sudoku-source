package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.Difficulty

@Entity(tableName = "game_history")
data class GameHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateCompleted: Long, // timestamp
    val timeTakenSeconds: Long,
    val difficulty: String, // String representation of Difficulty
    val mistakes: Int,
    val hintsUsed: Int,
    val isDaily: Boolean,
    val seedDate: String? // e.g. "2026-08-29" if daily
)

@Entity(tableName = "daily_streak")
data class DailyStreakEntity(
    @PrimaryKey val id: Int = 1, // Only one row
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedDateString: String? = null // e.g. "2026-08-29"
)

@Entity(tableName = "active_game")
data class ActiveGameEntity(
    @PrimaryKey val id: Int = 1, // Only one row
    val cellsJson: String, // JSON serialization of List<SudokuCell>
    val solutionJson: String, // JSON serialization of IntArray
    val difficulty: String,
    val timerSeconds: Long,
    val mistakes: Int,
    val hintsUsed: Int,
    val isDaily: Boolean,
    val seedDate: String?
)
