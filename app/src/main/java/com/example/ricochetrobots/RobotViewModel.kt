package com.example.ricochetrobots

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

// Robot state data class
data class RobotState(
    val id: Int,
    val currentPos: MutableState<Offset> = mutableStateOf(Offset.Zero),
    val targetPos: MutableState<Offset> = mutableStateOf(Offset.Zero),
    val animProgress: Animatable<Float, AnimationVector1D> = Animatable(1f)
)


class RobotViewModel : ViewModel() {
}
