package com.example.ricochetrobots.ui.theme.composable.gameboard

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.ricochetrobots.Direction
import kotlinx.coroutines.delay

@SuppressLint("Range")
@Composable
fun AnimatedGridMovement(robotViewModel: RobotViewModel) {

    val robots = robotViewModel.robots
    val coroutineScope = rememberCoroutineScope()
    var timeLeft by remember { mutableIntStateOf(120) } // 2 minutes

    var boardSizePx by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0 && !robotViewModel.isWinningCondition.value) {
            delay(1000)
            timeLeft--
        }
//        if (timeLeft == 0) {
////            robotViewModel.isSolutionDialogOpen.value = true
//        }
    }

    // Move function for each robot
    fun moveRobot(robotId: Int, newPos: Offset) {

        Log.d("kevin robot pos", "robot id: $robotId, new positon: ${newPos.x}, ${newPos.y}")

        val robot = robots.first { it.id == robotId }
        if (robot.animProgress.isRunning) return

        coroutineScope.launch {
            robot.prevPos.value = robot.targetPos.value
            robot.targetPos.value = newPos

            if (robot.targetPos.value != robot.prevPos.value){
                robotViewModel.numberOfMoves.value++
            }

            robot.animProgress.snapTo(0f)
            robot.animProgress.animateTo(1f, animationSpec = tween(durationMillis = 300))

            robotViewModel.checkIsWinningState()

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
    CustomDialog(robotViewModel.isSolutionDialogOpen.value, solutionList = robotViewModel.solution.value ?: emptyList(), onDismiss = {
        robotViewModel.isSolutionDialogOpen.value = false
    })

    WinnerDialog(
        robotViewModel.isWinningCondition.value,
        robotViewModel.isWinningConditionDialogOpen.value,
        robotViewModel.username.value ?: "") {
        robotViewModel.isWinningConditionDialogOpen.value = false
    }
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(WindowInsets.safeDrawing.asPaddingValues()) // respect system insets
        .padding(20.dp)) {
        Column (modifier = Modifier.fillMaxWidth()) {
            Row (modifier = Modifier.fillMaxWidth()){
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { robotViewModel.resetBoard() },
                    modifier = Modifier.size(64.dp) // adjust touch target size if needed
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset"
                    )
                }
                IconButton(
                    onClick = { robotViewModel.leaveGame() },
                    modifier = Modifier.size(64.dp) // adjust touch target size if needed
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit"
                    )
                }

            }
            Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("${timeLeft}s")
            }
            Row (modifier = Modifier.fillMaxWidth()){
                Text("Moves: ${robotViewModel.numberOfMoves.value}")
            }

            PlayerList(robotViewModel)

            if (robotViewModel.isWinningCondition.value){
                Button(onClick = {

                    println("kevin solution " + robotViewModel.solution.value)
                    robotViewModel.isSolutionDialogOpen.value = true
                }) { Text("Show Solution") }
            }
        }
        Column(
            Modifier
                .fillMaxSize()
        ) {
            Box(
                Modifier
                    .padding(top = 50.dp)
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
        GameControls(
            selectedRobot = selectedRobot,
            rows = robotViewModel.rows,
            columns = robotViewModel.columns,
            moveTo = { newPos, robotID -> moveRobot(robotID, newPos) },
            robotViewModel = robotViewModel
        )
    }

}

@Composable
fun PlayerList(robotViewModel: RobotViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        robotViewModel.players.value.forEach { player ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Green online indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.Green, CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Player name, append (you) if matches
                val displayName =
                    if (player == robotViewModel.username.value) "$player (you)" else player

                Text(displayName, color = Color.Black)

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    "${
                        if (robotViewModel.playerBestSolution[player] == 10000)
                            "No solution"
                        else
                            robotViewModel.playerBestSolution[player] ?: 0
                    }"
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomDialog(
    showDialog: Boolean,
    solutionList: List<Pair<Int, Direction>>,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        Dialog(onDismissRequest = { onDismiss() }) {
            Box(
                modifier = Modifier
                    .width(400.dp)
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        Text("Optimal Solution")
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { onDismiss() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.Start
                    ) {
                        solutionList.forEach { (robotId, direction) ->
                            val color = when (robotId) {
                                0 -> Color.Red
                                1 -> Color.Gray
                                2 -> Color.Black
                                else -> Color.Magenta
                            }
                            DisabledArrow(direction, color)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WinnerDialog(
    isWinningCondition: Boolean,
    isWinningConditionDialogOpen: Boolean,
    winningPlayer: String,
    onDismiss: () -> Unit
) {
    if (isWinningConditionDialogOpen) {
        Dialog(onDismissRequest = { onDismiss() }) {
            Box(
                modifier = Modifier
                    .width(400.dp)
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        Text("Winner")
                    }
                    Box {
                        Text(winningPlayer)
                    }

                }
            }
        }
    }
}

@Composable
private fun DisabledArrow(direction: Direction, tint: Color) {
    val icon = when (direction) {
        Direction.Up -> Icons.Default.KeyboardArrowUp
        Direction.Down -> Icons.Default.KeyboardArrowDown
        Direction.Left -> Icons.AutoMirrored.Filled.KeyboardArrowLeft
        Direction.Right -> Icons.AutoMirrored.Filled.KeyboardArrowRight
    }

    Icon(
        imageVector = icon,
        contentDescription = direction.name,
        tint = tint,
        modifier = Modifier.size(32.dp)
    )
}

