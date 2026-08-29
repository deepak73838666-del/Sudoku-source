package com.example.data.repository

import com.example.data.local.ActiveGameEntity
import com.example.data.local.DailyStreakEntity
import com.example.data.local.GameDao
import com.example.data.local.GameHistoryEntity
import com.example.data.models.Difficulty
import com.example.data.models.GameState
import com.example.data.models.SudokuCell
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val gameDao: GameDao) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    
    private val cellListType = Types.newParameterizedType(List::class.java, SudokuCell::class.java)
    private val cellListAdapter = moshi.adapter<List<SudokuCell>>(cellListType)
    private val intArrayAdapter = moshi.adapter(IntArray::class.java)

    fun getActiveGame(): Flow<GameState?> {
        return gameDao.getActiveGame().map { entity ->
            if (entity == null) null
            else {
                GameState(
                    cells = cellListAdapter.fromJson(entity.cellsJson) ?: emptyList(),
                    solution = intArrayAdapter.fromJson(entity.solutionJson) ?: IntArray(81),
                    difficulty = Difficulty.valueOf(entity.difficulty),
                    timerSeconds = entity.timerSeconds,
                    mistakes = entity.mistakes,
                    hintsUsed = entity.hintsUsed,
                    isCompleted = false,
                    isDaily = entity.isDaily,
                    seedDate = entity.seedDate
                )
            }
        }
    }

    suspend fun getActiveGameSync(): GameState? {
        val entity = gameDao.getActiveGameSync() ?: return null
        return GameState(
            cells = cellListAdapter.fromJson(entity.cellsJson) ?: emptyList(),
            solution = intArrayAdapter.fromJson(entity.solutionJson) ?: IntArray(81),
            difficulty = Difficulty.valueOf(entity.difficulty),
            timerSeconds = entity.timerSeconds,
            mistakes = entity.mistakes,
            hintsUsed = entity.hintsUsed,
            isCompleted = false,
            isDaily = entity.isDaily,
            seedDate = entity.seedDate
        )
    }

    suspend fun saveActiveGame(state: GameState) {
        val entity = ActiveGameEntity(
            cellsJson = cellListAdapter.toJson(state.cells),
            solutionJson = intArrayAdapter.toJson(state.solution),
            difficulty = state.difficulty.name,
            timerSeconds = state.timerSeconds,
            mistakes = state.mistakes,
            hintsUsed = state.hintsUsed,
            isDaily = state.isDaily,
            seedDate = state.seedDate
        )
        gameDao.saveActiveGame(entity)
    }

    suspend fun clearActiveGame() {
        gameDao.clearActiveGame()
    }

    suspend fun saveCompletedGame(history: GameHistoryEntity) {
        gameDao.insertHistory(history)
    }

    fun getAllHistory(): Flow<List<GameHistoryEntity>> {
        return gameDao.getAllHistory()
    }
    
    fun getDailyHistory(): Flow<List<GameHistoryEntity>> {
        return gameDao.getDailyHistory()
    }
    
    suspend fun getDailyHistoryByDate(date: String): GameHistoryEntity? {
        return gameDao.getDailyHistoryByDate(date)
    }

    fun getStreak(): Flow<DailyStreakEntity?> {
        return gameDao.getStreak()
    }

    suspend fun updateStreak(streak: DailyStreakEntity) {
        gameDao.updateStreak(streak)
    }
}
