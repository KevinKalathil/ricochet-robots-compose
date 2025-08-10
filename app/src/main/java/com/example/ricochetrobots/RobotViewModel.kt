package com.example.ricochetrobots

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlin.math.roundToInt

data class RobotState(
    val id: Int,
    var isSelected: MutableState<Boolean> = mutableStateOf(false),
    val currentPos: MutableState<Offset> = mutableStateOf(Offset.Zero),
    val targetPos: MutableState<Offset> = mutableStateOf(Offset.Zero),
    val animProgress: Animatable<Float, AnimationVector1D> = Animatable(1f)
)

class RobotViewModel : ViewModel() {
    val columns = 8
    val rows = 12
    var robots by mutableStateOf(
        List(3) { index ->
            RobotState(id = index)
        }
    )
        private set

    init {
        generateRandomPositions()
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
    }

    // Helper to get currently selected robot
    fun getSelectedRobot(): RobotState? {
        return robots.firstOrNull { it.isSelected.value }
    }

    // Check if a position is free (no other robot there)
    private fun isPositionFree(x: Int, y: Int, ignoreRobotId: Int): Boolean {
        return robots.none {
            it.id != ignoreRobotId &&
                    it.targetPos.value.x.roundToInt() == x &&
                    it.targetPos.value.y.roundToInt() == y
        }
    }


    fun getRightmostAvailableBox(gridWidth: Int = columns): Float? {
        val robot = getSelectedRobot() ?: return null
        var x = robot.targetPos.value.x.toInt()
        val y = robot.targetPos.value.y.toInt()

        // Move right until blocked or end of grid
        while (x + 1 < gridWidth && isPositionFree(x + 1, y, robot.id)) {
            x++
        }
        return x.toFloat()
    }

    fun getLeftmostAvailableBox(): Float? {
        val robot = getSelectedRobot() ?: return null
        var x = robot.targetPos.value.x.toInt()
        val y = robot.targetPos.value.y.toInt()

        while (x - 1 >= 0 && isPositionFree(x - 1, y, robot.id)) {
            x--
        }
        return x.toFloat()
    }

    fun getTopmostAvailableBox(): Float? {
        val robot = getSelectedRobot() ?: return null
        val x = robot.targetPos.value.x.toInt()
        var y = robot.targetPos.value.y.toInt()

        while (y - 1 >= 0 && isPositionFree(x, y - 1, robot.id)) {
            y--
        }
        return y.toFloat()
    }

    fun getBottommostAvailableBox(gridHeight: Int = rows): Float? {
        val robot = getSelectedRobot() ?: return null
        val x = robot.targetPos.value.x.toInt()
        var y = robot.targetPos.value.y.toInt()

        while (y + 1 < gridHeight && isPositionFree(x, y + 1, robot.id)) {
            y++
        }
        return y.toFloat()
    }
}

