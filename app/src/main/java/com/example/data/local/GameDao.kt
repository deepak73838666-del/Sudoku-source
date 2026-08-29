package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM game_history ORDER BY dateCompleted DESC")
    fun getAllHistory(): Flow<List<GameHistoryEntity>>

    @Insert
    suspend fun insertHistory(history: GameHistoryEntity)

    @Query("SELECT * FROM game_history WHERE isDaily = 1")
    fun getDailyHistory(): Flow<List<GameHistoryEntity>>

    @Query("SELECT * FROM game_history WHERE isDaily = 1 AND seedDate = :date LIMIT 1")
    suspend fun getDailyHistoryByDate(date: String): GameHistoryEntity?

    @Query("SELECT * FROM daily_streak WHERE id = 1")
    fun getStreak(): Flow<DailyStreakEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStreak(streak: DailyStreakEntity)

    @Query("SELECT * FROM active_game WHERE id = 1")
    fun getActiveGame(): Flow<ActiveGameEntity?>

    @Query("SELECT * FROM active_game WHERE id = 1")
    suspend fun getActiveGameSync(): ActiveGameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActiveGame(game: ActiveGameEntity)

    @Query("DELETE FROM active_game WHERE id = 1")
    suspend fun clearActiveGame()
}
