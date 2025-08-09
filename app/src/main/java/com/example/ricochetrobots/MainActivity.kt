package com.example.ricochetrobots

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.ricochetrobots.ui.theme.RicochetRobotsTheme
import kotlinx.coroutines.delay

// --- Constants ---
private const val BOARD_ROWS = 5
private const val BOARD_COLS = 5
private val BOX_SIZE = 50.dp
private val BOARD_PADDING = 10.dp
private val BOX_PADDING = 10.dp

class MainActivity : ComponentActivity() {

    private val robotViewModel = RobotViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RicochetRobotsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        GameBoard(robotViewModel = robotViewModel)
                    }
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Robot(robotViewModel = robotViewModel)
                    }
                }
            }
        }
    }
}

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun Robot(robotViewModel: RobotViewModel) {
    val boardSize by robotViewModel.gameBoardSize.collectAsState()
    val robotState by robotViewModel.robotState.collectAsState()
    val shift = BOARD_PADDING.value + BOX_PADDING.value

    if (boardSize.widthDp > 0.dp && boardSize.heightDp > 0.dp) {
        val colShift = (boardSize.widthDp - (shift/2).dp) / BOARD_COLS
        val rowShift = (boardSize.heightDp - (shift/2).dp ) / BOARD_ROWS

        val animatedX = remember {
            Animatable((robotState.gridPosCol * colShift.value) + shift)
        }
        val animatedY = remember {
            Animatable((robotState.gridPosRow * rowShift.value) + shift)
        }

        LaunchedEffect(Unit) {
            delay(2000)
            robotViewModel.moveTo(1, 1)
            delay(2000)
            robotViewModel.moveTo(0, 0)
        }

        LaunchedEffect(robotState) {
            val targetX = (robotState.gridPosCol * colShift.value) + shift
            val targetY = (robotState.gridPosRow * rowShift.value) + shift
            animatedX.animateTo(targetX, tween(durationMillis = 500))
            animatedY.animateTo(targetY, tween(durationMillis = 500))
        }

        Box(
            modifier = Modifier
                .offset(animatedX.value.dp, animatedY.value.dp)
                .size(BOX_SIZE)
                .background(Color.Red)
        )
    }
}

@Composable
fun GameBoard(robotViewModel: RobotViewModel) {
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .padding(BOARD_PADDING)
                .onGloballyPositioned { coordinates ->
                    val sizePx = coordinates.size
                    val sizeDpWidth = with(density) { sizePx.width.toDp() }
                    val sizeDpHeight = with(density) { sizePx.height.toDp() }
                    Log.d(
                        "kevin",
                        "${sizePx.width} x ${sizePx.height} $sizeDpHeight x $sizeDpWidth "
                    )

                    robotViewModel.setNewBoardSize(sizeDpWidth, sizeDpHeight)
                }
        ) {
            repeat(BOARD_ROWS) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Blue)
                        .padding(BOX_PADDING),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(BOARD_COLS) {
                        Box(
                            modifier = Modifier
                                .size(BOX_SIZE)
                                .aspectRatio(1f)
                                .background(Color.Cyan)
                        )
                    }
                }
            }
        }
    }
}
