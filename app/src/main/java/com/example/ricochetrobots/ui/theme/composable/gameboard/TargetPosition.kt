package com.example.ricochetrobots.ui.theme.composable.gameboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp


@Composable
fun TargetPosition(
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
