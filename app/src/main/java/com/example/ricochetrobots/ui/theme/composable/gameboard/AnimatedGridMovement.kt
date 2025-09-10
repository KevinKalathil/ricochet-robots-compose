package com.example.ricochetrobots.ui.theme.composable.gameboard

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.ricochetrobots.RobotViewModel
import kotlinx.coroutines.launch

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

    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {

        Column(
            Modifier
                .fillMaxSize()
        ) {
            Box(
                Modifier
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
                    TargetPosition(robotViewModel.targetPos.value, cellWidthPx = cellWidthPx, cellHeightPx = cellHeightPx)

                    // Draw each robot
                    robots.forEach { robot ->
                        Robot(
                            id = robot.id,
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
        }
        val selectedRobot = robotViewModel.getSelectedRobot()

        // Example Controls for the first robot (id=0)
        selectedRobot?.targetPos?.let {
            GameControls(
                targetPos = it.value,
                rows = robotViewModel.rows,
                columns = robotViewModel.columns,
                moveTo = { newPos -> moveRobot(selectedRobot.id, newPos) },
                robotViewModel = robotViewModel
            )
        }
    }

}

