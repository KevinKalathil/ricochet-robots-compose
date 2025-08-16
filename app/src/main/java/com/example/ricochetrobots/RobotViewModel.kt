package com.example.ricochetrobots

import android.service.quicksettings.Tile
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

data class RobotState(
    val id: Int,
    var isSelected: MutableState<Boolean> = mutableStateOf(false),
    val currentPos: MutableState<Offset> = mutableStateOf(Offset.Zero),
    val targetPos: MutableState<Offset> = mutableStateOf(Offset.Zero),
    val animProgress: Animatable<Float, AnimationVector1D> = Animatable(1f)
)

data class TileBlockState(
    val gridState: MutableState<Array<Array<Int>>> = mutableStateOf(emptyArray()),
)

enum class Direction {
    Right,
    Left,
    Up,
    Down
}

class RobotViewModel : ViewModel() {
    val columns = 10
    val rows = 10
    var robots by mutableStateOf(
        List(3) { index ->
            RobotState(id = index)
        }
    )
        private set

    var tileBlockState: TileBlockState = TileBlockState()

    var targetPos: MutableState<Offset> = mutableStateOf(Offset.Zero)

    init {
        generateRandomPositions()
        generateRandomTileBlocks()
        val grid = tileBlockState.gridState.value
        val gridString = buildString {
            for (row in grid) {
                append(row.joinToString(separator = " ") { it.toString() })
                append("\n")
            }
        }
        Log.d("Kevin tile grid", "\n$gridString")

    }

    private fun getMinManhattanDistanceToTarget(): Int {
        var minDistance = 1000
        robots.forEach { robot ->
            minDistance = min(
                minDistance,
                (abs(robot.targetPos.value.x - targetPos.value.x) + abs(robot.targetPos.value.y - targetPos.value.y)).toInt()
            )
        }

        return minDistance
    }

    private fun generateRandomPositions() {
        robots.forEach { robot ->
            robot.currentPos.value = Offset(
                x = (0..<columns).random().toFloat(),
                y = (0..<rows).random().toFloat()
            )
            robot.targetPos.value = Offset(
                x = robot.currentPos.value.x,
                y = robot.currentPos.value.y,
            )
        }

        // ensure manhattan distance between any robot and the target is at least 3
        do {
            targetPos.value = Offset(
                x = (0..columns).random().toFloat(),
                y = (0..rows).random().toFloat()
            )
            Log.d("kevin target pos generation", "${targetPos.value}")
        } while (getMinManhattanDistanceToTarget() < 3)
    }

    private fun isWallExisting(wallProbability: Double): Boolean {
        return (0..100).random() / 100.0 < wallProbability
    }

    fun generateRandomTileBlocks() {
        val grid = Array(rows) { Array(columns) { 0 } }

        val wallProbability = 0.1 // chance to add a wall on each side

        for (y in 0 until rows) {
            for (x in 0 until columns) {
                var cell = 0
                // North wall
                if (y == 0 || isWallExisting(wallProbability)) {
                    cell = cell or 1
                }
                // West wall
                if (x == 0 || isWallExisting(wallProbability)) {
                    cell = cell or 8
                }

                grid[y][x] = cell
            }
        }

        // Ensure robot starting positions are free (no walls blocking)
        robots.forEach { robot ->
            val col = robot.currentPos.value.x.toInt().coerceIn(0, columns - 1)
            val row = robot.currentPos.value.y.toInt().coerceIn(0, rows - 1)
            grid[row][col] = 0
        }

        // set top/bottom border
        for (x in 0 until columns) {
            grid[0][x] = grid[0][x] or 1
            grid[rows-1][x] = grid[rows-1][x] or 4
        }
        // set left/right border
        for (y in 0 until rows) {
            grid[y][0] = grid[y][0] or 8
            grid[y][columns - 1] = grid[y][columns - 1] or 2
        }

        tileBlockState.gridState.value = grid
    }


    // Helper to get currently selected robot
    fun getSelectedRobot(): RobotState? {
        return robots.firstOrNull { it.isSelected.value }
    }

    // Check if a position is free (no other robot there)
    private fun isPositionFree(x: Int, y: Int, ignoreRobotId: Int, direction: Direction): Boolean {
        val noRobotsInSpace = robots.none {
            it.id != ignoreRobotId &&
                    it.targetPos.value.x.roundToInt() == x &&
                    it.targetPos.value.y.roundToInt() == y
        }

        val isWallBlocking = when (direction){
            Direction.Up -> {
                // check if bottom block's top side is blocked
                (tileBlockState.gridState.value[y+1][x] and 1) != 0
            }
            Direction.Right -> {
                // check if left is blocked
                (tileBlockState.gridState.value[y][x] and 8) != 0
            }
            Direction.Down -> {
                // check if top is blocked
                (tileBlockState.gridState.value[y][x] and 1) != 0
            }
            Direction.Left -> {
                // check if right block's left side is blocked
                (tileBlockState.gridState.value[y][x+1] and 8) != 0
            }
        }

        return (noRobotsInSpace and !isWallBlocking)
    }


    fun getRightmostAvailableBox(gridWidth: Int = columns): Float? {
        val robot = getSelectedRobot() ?: return null
        var x = robot.targetPos.value.x.toInt()
        val y = robot.targetPos.value.y.toInt()

        // Move right until blocked or end of grid
        while (x + 1 < gridWidth && isPositionFree(x + 1, y, robot.id, Direction.Right)) {
            x++
        }
        return x.toFloat()
    }

    fun getLeftmostAvailableBox(): Float? {
        val robot = getSelectedRobot() ?: return null
        var x = robot.targetPos.value.x.toInt()
        val y = robot.targetPos.value.y.toInt()

        while (x - 1 >= 0 && isPositionFree(x - 1, y, robot.id, Direction.Left)) {
            x--
        }
        return x.toFloat()
    }

    fun getTopmostAvailableBox(): Float? {
        val robot = getSelectedRobot() ?: return null
        val x = robot.targetPos.value.x.toInt()
        var y = robot.targetPos.value.y.toInt()

        while (y - 1 >= 0 && isPositionFree(x, y - 1, robot.id, Direction.Up)) {
            y--
        }
        return y.toFloat()
    }

    fun getBottommostAvailableBox(gridHeight: Int = rows): Float? {
        val robot = getSelectedRobot() ?: return null
        val x = robot.targetPos.value.x.toInt()
        var y = robot.targetPos.value.y.toInt()

        while (y + 1 < gridHeight && isPositionFree(x, y + 1, robot.id, Direction.Down)) {
            y++
        }
        return y.toFloat()
    }
}

