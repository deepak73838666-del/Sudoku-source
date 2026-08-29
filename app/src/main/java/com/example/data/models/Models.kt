package com.example.data.models

enum class Difficulty(val targetClues: Int) {
    EASY(42), // ~40-45
    MEDIUM(35), // ~32-39
    HARD(29), // ~27-31
    EXPERT(24) // ~22-26
}

data class SudokuCell(
    val index: Int,
    val value: Int = 0,
    val isGiven: Boolean = false,
    val notes: Set<Int> = emptySet(),
    val isError: Boolean = false
) {
    val row: Int get() = index / 9
    val col: Int get() = index % 9
}

data class GameState(
    val cells: List<SudokuCell>,
    val solution: IntArray,
    val difficulty: Difficulty,
    val timerSeconds: Long = 0,
    val mistakes: Int = 0,
    val hintsUsed: Int = 0,
    val isCompleted: Boolean = false,
    val isDaily: Boolean = false,
    val seedDate: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (cells != other.cells) return false
        if (!solution.contentEquals(other.solution)) return false
        if (difficulty != other.difficulty) return false
        if (timerSeconds != other.timerSeconds) return false
        if (mistakes != other.mistakes) return false
        if (hintsUsed != other.hintsUsed) return false
        if (isCompleted != other.isCompleted) return false
        if (isDaily != other.isDaily) return false
        if (seedDate != other.seedDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cells.hashCode()
        result = 31 * result + solution.contentHashCode()
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + timerSeconds.hashCode()
        result = 31 * result + mistakes
        result = 31 * result + hintsUsed
        result = 31 * result + isCompleted.hashCode()
        result = 31 * result + isDaily.hashCode()
        result = 31 * result + (seedDate?.hashCode() ?: 0)
        return result
    }
}
