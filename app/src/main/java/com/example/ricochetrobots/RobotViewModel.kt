import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel

data class RobotState(
    val id: Int,
    val currentPos: MutableState<Offset> = mutableStateOf(Offset.Zero),
    val targetPos: MutableState<Offset> = mutableStateOf(Offset.Zero),
    val animProgress: Animatable<Float, AnimationVector1D> = Animatable(1f)
)

class RobotViewModel : ViewModel() {
    // List of robots tracked as a mutable state
    var robots by mutableStateOf(
        List(3) { index ->
            RobotState(id = index)
        }
    )
        private set

    fun generateRandomPositions() {
        robots.forEach { robot ->
            robot.currentPos.value = Offset(
                x = (0..7).random().toFloat(),
                y = (0..15).random().toFloat()
            )
            robot.targetPos.value = Offset(
                x = (0..7).random().toFloat(),
                y = (0..15).random().toFloat()
            )
        }
    }

    fun moveRobotTo(robotId: Int, newTargetPos: Offset) {
        val robot = robots.find { it.id == robotId } ?: return
        robot.targetPos.value = newTargetPos
    }
}
