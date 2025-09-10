package com.example.ricochetrobots.ui.theme.composable.gameboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt


@Composable
fun Robot(
    id: Int,
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
    val color = when (id) {
        0 -> Color.Red
        1 -> Color.Gray
        2 -> Color.Black
        else -> Color.Magenta
    }

    Box(
        Modifier
            .offset { IntOffset(animatedX.roundToInt(), animatedY.roundToInt()) }
            .size(with(density) { cellWidthPx.toDp() }, with(density) { cellHeightPx.toDp() })
            .padding(6.dp)
            .background(if (isSelected) Color.Green else color)
            .clickable { onClick() }
            , contentAlignment = Alignment.Center
    ) {
        Text(text = id.toString(), textAlign = TextAlign.Center, color = Color.White,  fontWeight = FontWeight.SemiBold)
    }
}

