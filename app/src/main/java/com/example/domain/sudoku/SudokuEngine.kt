package com.example.domain.sudoku

import kotlin.random.Random

object SudokuEngine {

    const val SIZE = 9
    const val BOX_SIZE = 3

    // Generates a valid full board, then removes cells to create a puzzle with a unique solution
    fun generatePuzzle(clues: Int, seed: Long? = null): Pair<IntArray, IntArray> {
        val random = if (seed != null) Random(seed) else Random.Default
        val solution = IntArray(SIZE * SIZE)
        fillBoard(solution, random)

        val puzzle = solution.clone()
        val cellIndices = (0 until SIZE * SIZE).toMutableList()
        cellIndices.shuffle(random)

        var currentClues = SIZE * SIZE

        for (index in cellIndices) {
            if (currentClues <= clues) break

            val temp = puzzle[index]
            puzzle[index] = 0

            if (!hasUniqueSolution(puzzle)) {
                // If removing this breaks uniqueness, put it back
                puzzle[index] = temp
            } else {
                currentClues--
            }
        }

        return Pair(puzzle, solution)
    }

    private fun fillBoard(board: IntArray, random: Random): Boolean {
        for (i in 0 until SIZE * SIZE) {
            if (board[i] == 0) {
                val numbers = (1..9).toMutableList()
                numbers.shuffle(random)
                for (num in numbers) {
                    if (isValidMove(board, i, num)) {
                        board[i] = num
                        if (fillBoard(board, random)) return true
                        board[i] = 0
                    }
                }
                return false
            }
        }
        return true
    }

    fun isValidMove(board: IntArray, index: Int, num: Int): Boolean {
        val row = index / SIZE
        val col = index % SIZE

        // Check row
        for (c in 0 until SIZE) {
            if (board[row * SIZE + c] == num) return false
        }

        // Check col
        for (r in 0 until SIZE) {
            if (board[r * SIZE + col] == num) return false
        }

        // Check box
        val startRow = (row / BOX_SIZE) * BOX_SIZE
        val startCol = (col / BOX_SIZE) * BOX_SIZE
        for (r in 0 until BOX_SIZE) {
            for (c in 0 until BOX_SIZE) {
                if (board[(startRow + r) * SIZE + (startCol + c)] == num) return false
            }
        }

        return true
    }

    private fun hasUniqueSolution(board: IntArray): Boolean {
        return countSolutions(board, 0, 0) == 1
    }

    private fun countSolutions(board: IntArray, index: Int, count: Int): Int {
        var currentCount = count
        if (index == SIZE * SIZE) {
            return currentCount + 1
        }

        if (board[index] != 0) {
            return countSolutions(board, index + 1, currentCount)
        }

        for (num in 1..9) {
            if (isValidMove(board, index, num)) {
                board[index] = num
                currentCount = countSolutions(board, index + 1, currentCount)
                board[index] = 0
                if (currentCount > 1) return currentCount // Early exit if more than 1 solution
            }
        }
        return currentCount
    }
}
