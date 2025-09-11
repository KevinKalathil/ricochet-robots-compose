package com.example.ricochetrobots.ui.theme.composable.gameboard

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun GameBoard(rows: Int, columns: Int, tileBlockState: Array<Array<Int>>) {
    BoxWithConstraints(modifier = Modifier, contentAlignment = Alignment.Center) {
        // maxWidth is the total available width for the board
        val cellSize = maxWidth / columns

        // 🎨 Color scheme
        val cellBackground = Color(0xFFF5F5F5) // light neutral
        val gridLineColor = Color(0xFFDDDDDD)  // subtle gray
        val wallColor = Color(0xFF444444)      // strong contrast

        Column {
            repeat(rows) { rowIndex ->
                Row {
                    repeat(columns) { colIndex ->
                        val value = tileBlockState[rowIndex][colIndex]

                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .background(cellBackground)
                                .drawWithContent {
                                    // Draw grid lines
                                    drawRect(
                                        color = gridLineColor,
                                        style = Stroke(width = 8f)
                                    )

                                    val strokeWidth = 12f
                                    val isTopWallBlocked = (value and 1) != 0
                                    val isRightWallBlocked = (value and 2) != 0
                                    val isBottomWallBlocked = (value and 4) != 0
                                    val isLeftWallBlocked = (value and 8) != 0

                                    // Walls
                                    if (isTopWallBlocked) {
                                        drawLine(
                                            color = wallColor,
                                            start = Offset(0f, 0f),
                                            end = Offset(size.width, 0f),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                                    if (isRightWallBlocked) {
                                        drawLine(
                                            color = wallColor,
                                            start = Offset(size.width, 0f),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                                    if (isBottomWallBlocked) {
                                        drawLine(
                                            color = wallColor,
                                            start = Offset(0f, size.height),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                                    if (isLeftWallBlocked) {
                                        drawLine(
                                            color = wallColor,
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
