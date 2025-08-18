package com.example.ricochetrobots

import android.annotation.SuppressLint
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
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.Stroke

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


@SuppressLint("Range")
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
            robot.prevPos.value = robot.targetPos.value
            robot.targetPos.value = newPos
            robot.animProgress.snapTo(0f)
            robot.animProgress.animateTo(1f, animationSpec = tween(durationMillis = 300))

            // Log all robot positions after updating
            robots.forEach { r ->
                Log.d(
                    "kevin all robots",
                    "robot ${r.id}: current=(${r.prevPos.value.x}, ${r.prevPos.value.y}), " +
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
                .aspectRatio(robotViewModel.columns.toFloat() / robotViewModel.rows.toFloat())
                .onGloballyPositioned { coordinates -> boardSizePx = coordinates.size }
                .background(Color.LightGray),
            contentAlignment = Alignment.TopStart
        ) {
            GameBoard(robotViewModel.rows, robotViewModel.columns, robotViewModel.tileBlockState.gridState.value)
            if (boardSizePx.width > 0 && boardSizePx.height > 0) {
                val cellWidthPx = boardSizePx.width.toFloat() / robotViewModel.columns
                val cellHeightPx = boardSizePx.height.toFloat() / robotViewModel.rows
                Target(robotViewModel.targetPos.value, cellWidthPx = cellWidthPx, cellHeightPx = cellHeightPx)

                // Draw each robot
                robots.forEach { robot ->
                    Robot(
                        animProgress = robot.animProgress,
                        prevPos = robot.prevPos.value,
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
    prevPos: Offset,
    targetPos: Offset,
    cellWidthPx: Float,
    cellHeightPx: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val animatedX = ((1 - animProgress.value) * prevPos.x + animProgress.value * targetPos.x) * cellWidthPx
    val animatedY = ((1 - animProgress.value) * prevPos.y + animProgress.value * targetPos.y) * cellHeightPx

    Box(
        Modifier
            .offset { IntOffset(animatedX.roundToInt(), animatedY.roundToInt()) }
            .size(with(density) { cellWidthPx.toDp() }, with(density) { cellHeightPx.toDp() })
            .padding(6.dp)
            .background(if (isSelected) Color.Green else Color.Red)
            .clickable { onClick() }
    )
}

@Composable
fun Target(
    pos: Offset,
    cellWidthPx: Float,
    cellHeightPx: Float,
) {
    val density = LocalDensity.current
    val offsetX = (pos.x * cellWidthPx).toInt()
    val offsetY = (pos.y * cellHeightPx).toInt()
    Box(
        Modifier
            .offset { IntOffset(offsetX, offsetY) }
            .size(with(density) { cellWidthPx.toDp() }, with(density) { cellHeightPx.toDp() })
            .padding(6.dp)
            .background(Color.Blue)
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
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                val newPosition = (robotViewModel.getNextAvailableBox(Direction.Left))
                val newX = newPosition?.x?.coerceAtLeast(0f)
                val newY = targetPos.y
                moveTo(Offset(newX ?: 0f, newY))
            }) { Text("Left") }

            Button(onClick = {
                val newPosition = (robotViewModel.getNextAvailableBox(Direction.Right))
                val newX = newPosition?.x?.coerceAtMost(columns - 1f)
                val newY = targetPos.y
                moveTo(Offset(newX ?: 0f, newY))
            }) { Text("Right") }

            Button(onClick = {
                val newPosition = (robotViewModel.getNextAvailableBox(Direction.Up))
                val newX = targetPos.x
                val newY = newPosition?.y?.coerceAtLeast(0f)
                moveTo(Offset(newX, newY ?: 0f))
            }) { Text("Up") }

            Button(onClick = {
                val newPosition = (robotViewModel.getNextAvailableBox(Direction.Down))
                val newX = targetPos.x
                val newY = newPosition?.y?.coerceAtMost(rows - 1f)

                moveTo(Offset(newX, newY ?: 0f))
            }) { Text("Down") }

        }
        Row {
            Button(onClick = {
                robotViewModel.generateRandomTileBlocks()
            }) { Text("Regenerate") }

            Button(onClick = {
                robotViewModel.resetBoard()
            }) { Text("Reset") }

            Button(onClick = {
                robotViewModel.solve()
            }) { Text("Solve") }
        }
        Row {
            Button(onClick = {
                robotViewModel.initNewPositions()
            }) { Text("Init") }
            Text(robotViewModel.getSelectedRobot()?.id.toString())
        }

    }
}


@Composable
fun GameBoard(rows: Int, columns: Int, tileBlockState: Array<Array<Int>>) {
    BoxWithConstraints(modifier = Modifier) {
        // maxWidth is the total available width for the board
        val cellSize = maxWidth / columns

        Column {
            repeat(rows) { rowIndex ->
                Row {
                    repeat(columns) { colIndex ->
                        val value = tileBlockState[rowIndex][colIndex]

                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .background(Color.Black)
//                                .border(2.dp, Color.LightGray)
                                .drawWithContent {
                                    drawRect(
                                        color = Color.LightGray,
                                        style = Stroke(width = 4f)
                                    )
                                    val strokeWidth = 15f
                                    val isTopWallBlocked = (value and 1) != 0
                                    val isRightWallBlocked = (value and 2) != 0
                                    val isBottomWallBlocked = (value and 4) != 0
                                    val isLeftWallBlocked = (value and 8) != 0

                                    val borderColor = Color.Magenta

                                    // Top wall: draw if top row or top wall exists
                                    if (isTopWallBlocked) {
                                        drawLine(
                                            color = borderColor,
                                            start = Offset(0f, 0f),
                                            end = Offset(size.width, 0f),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                                    // Right wall: only draw if rightmost column OR adjacent cell to the right doesn't have left wall
                                    if (isRightWallBlocked) {
                                        drawLine(
                                            color = borderColor,
                                            start = Offset(size.width, 0f),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                                    // Bottom wall: only draw if bottom row OR adjacent cell above doesn't have bottom wall
                                    if (isBottomWallBlocked) {
                                        drawLine(
                                            color = borderColor,
                                            start = Offset(0f, size.height),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                                    // Left wall: draw if left column or left wall exists
                                    if (isLeftWallBlocked) {
                                        drawLine(
                                            color = borderColor,
                                            start = Offset(0f, 0f),
                                            end = Offset(0f, size.height),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                                }
                        )

                    }
                }
            }
        }

    }
}


