package com.example.ricochetrobots

import RobotViewModel
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {

    private val robotViewModel: RobotViewModel = RobotViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    AnimatedGridMovement(robotViewModel)
                }
            }
        }
    }
}


@Composable
fun AnimatedGridMovement(robotViewModel: RobotViewModel) {

    val robots = robotViewModel.robots
    val coroutineScope = rememberCoroutineScope()

    var boardSizePx by remember { mutableStateOf(IntSize.Zero) }

    // Move function for each robot
    fun moveRobot(robotId: Int, newPos: Offset) {

        Log.d("kevin robot pos", "robot id: $robotId, new positon: ${newPos.x}, ${newPos.y}")

        val robot = robots.first { it.id == robotId }
        if (robot.animProgress.isRunning) return

        coroutineScope.launch {
            robot.currentPos.value = robot.targetPos.value
            robot.targetPos.value = newPos
            robot.animProgress.snapTo(0f)
            robot.animProgress.animateTo(1f, animationSpec = tween(durationMillis = 300))

            // Log all robot positions after updating
            robots.forEach { r ->
                Log.d(
                    "kevin all robots",
                    "robot ${r.id}: current=(${r.currentPos.value.x}, ${r.currentPos.value.y}), " +
                            "target=(${r.targetPos.value.x}, ${r.targetPos.value.y})"
                )
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Box(
            Modifier
                .padding(20.dp)
                .weight(1f)
                .fillMaxWidth()
                .aspectRatio(0.5f)
                .onGloballyPositioned { coordinates -> boardSizePx = coordinates.size }
                .background(Color.LightGray),
            contentAlignment = Alignment.TopStart
        ) {
            GameBoard(robotViewModel.rows, robotViewModel.columns)

            if (boardSizePx.width > 0 && boardSizePx.height > 0) {
                val cellWidthPx = boardSizePx.width.toFloat() / robotViewModel.columns
                val cellHeightPx = boardSizePx.height.toFloat() / robotViewModel.rows

                // Draw each robot
                robots.forEach { robot ->
                    Robot(
                        animProgress = robot.animProgress,
                        currentPos = robot.currentPos.value,
                        targetPos = robot.targetPos.value,
                        cellWidthPx = cellWidthPx,
                        cellHeightPx = cellHeightPx,
                        isSelected = robot.isSelected.value
                    ) {
                        robots.forEach { it.isSelected.value = false }
                        robot.isSelected.value = true
                    }
                }
            }
        }

        val selectedRobot = robotViewModel.getSelectedRobot()

        // Example Controls for the first robot (id=0)
        selectedRobot?.targetPos?.let {
            Controls(
                targetPos = it.value,
                rows = robotViewModel.rows,
                columns = robotViewModel.columns,
                moveTo = { newPos -> moveRobot(selectedRobot.id, newPos) },
                robotViewModel = robotViewModel
            )
        }
    }
}


@Composable
fun Robot(
    animProgress: Animatable<Float, AnimationVector1D>,
    currentPos: Offset,
    targetPos: Offset,
    cellWidthPx: Float,
    cellHeightPx: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val animatedX = ((1 - animProgress.value) * currentPos.x + animProgress.value * targetPos.x) * cellWidthPx
    val animatedY = ((1 - animProgress.value) * currentPos.y + animProgress.value * targetPos.y) * cellHeightPx

    Box(
        Modifier
            .offset { IntOffset(animatedX.roundToInt(), animatedY.roundToInt()) }
            .size(with(density) { cellWidthPx.toDp() }, with(density) { cellHeightPx.toDp() })
            .padding(6.dp)
            .background(if (isSelected) Color.Green else Color.Red) // optional visual feedback
            .clickable { onClick() }
    )
}


@Composable
fun Controls(
    robotViewModel: RobotViewModel,
    targetPos: Offset,
    rows: Int,
    columns: Int,
    moveTo: (Offset) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(onClick = {
            val newX = (robotViewModel.getLeftmostAvailableBox())?.coerceAtLeast(0f)
            val newY = targetPos.y
            moveTo(Offset(newX ?: 0f, newY))
        }) { Text("Left") }

        Button(onClick = {
            val newX = (robotViewModel.getRightmostAvailableBox())?.coerceAtMost(columns - 1f)
            val newY = targetPos.y
            moveTo(Offset(newX ?: 0f, newY))
        }) { Text("Right") }

        Button(onClick = {
            val newX = targetPos.x
            val newY = (robotViewModel.getTopmostAvailableBox())?.coerceAtLeast(0f)
            moveTo(Offset(newX, newY ?: 0f))
        }) { Text("Up") }

        Button(onClick = {
            val newX = targetPos.x
            val newY = (robotViewModel.getBottommostAvailableBox())?.coerceAtMost(rows - 1f)
            moveTo(Offset(newX, newY ?: 0f))
        }) { Text("Down") }
    }
}


@Composable
fun GameBoard(rows: Int, columns: Int, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        // maxWidth is the total available width for the board
        val cellSize = maxWidth / columns

        Column {
            repeat(rows) {
                Row {
                    repeat(columns) {
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .background(Color.Cyan)
                                .border(1.dp, Color.Black)
                        )
                    }
                }
            }
        }
    }
}


