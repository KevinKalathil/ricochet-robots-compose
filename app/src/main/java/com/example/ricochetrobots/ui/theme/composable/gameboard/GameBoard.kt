package com.example.ricochetrobots.ui.theme.composable.gameboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

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