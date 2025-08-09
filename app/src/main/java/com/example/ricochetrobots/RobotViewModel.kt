package com.example.ricochetrobots

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

// Robot state data class
data class RobotState(
    val gridPosRow: Int = 4,
    val gridPosCol: Int = 4
)

data class BoardSize(
    val widthDp: Dp = 0.dp,
    val heightDp: Dp = 0.dp
)


data class BoardSizePx(
    val widthPx: Float = 0f,
    val heightPx: Float = 0f
)

class RobotViewModel : ViewModel() {

    // Internal state flows
    private val _robotState = MutableStateFlow(RobotState())
    private val _gameBoardSize = MutableStateFlow(BoardSize())

    // Public immutable state
    val robotState: StateFlow<RobotState> = _robotState
    val gameBoardSize: StateFlow<BoardSize> = _gameBoardSize

    // Add this state for board size in pixels
    private val _boardSizePx = MutableStateFlow(BoardSizePx(0f, 0f))
    val boardSizePx: StateFlow<BoardSizePx> = _boardSizePx

    fun setNewBoardSizePx(widthPx: Float, heightPx: Float) {
        _boardSizePx.value = BoardSizePx(widthPx, heightPx)
    }

    // Update board size (e.g. from onGloballyPositioned)
    fun setNewBoardSize(widthDp: Dp, heightDp: Dp) {
        _gameBoardSize.value = BoardSize(widthDp, heightDp)
    }

    // Set position directly
    fun moveTo(newRow: Int, newCol: Int) {
        _robotState.update { current ->
            current.copy(gridPosRow = newRow, gridPosCol = newCol)
        }
    }

    // Move relative to current position
    fun moveBy(dRow: Int, dCol: Int) {
        _robotState.update { current ->
            current.copy(
                gridPosRow = current.gridPosRow + dRow,
                gridPosCol = current.gridPosCol + dCol
            )
        }
    }
}
