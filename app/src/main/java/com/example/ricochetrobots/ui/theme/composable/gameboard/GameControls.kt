package com.example.ricochetrobots.ui.theme.composable.gameboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.ricochetrobots.Direction
import com.example.ricochetrobots.RobotState
import com.example.ricochetrobots.RobotViewModel

@Composable
fun GameControls(
    selectedRobot: RobotState?,
    robotViewModel: RobotViewModel,
    rows: Int,
    columns: Int,
    moveTo: (Offset, Int) -> Unit,
) {
    val targetPos = selectedRobot?.targetPos?.value ?: Offset.Zero
    val robotID = selectedRobot?.id ?: -1

    Column {
        Spacer(modifier = Modifier.weight(1f)) // pushes content to bottom

        val arrowSize = 64.dp  // change this for bigger/smaller arrows

        if (robotViewModel.getSelectedRobot() == null ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Text("To control a robot, select one by touching it and then use the arrow keys that appear")
            }
        }
        else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row {
                    IconButton(
                        onClick = {
                            val newPosition = robotViewModel.getNextAvailableBox(Direction.Up)
                            val newX = targetPos.x
                            val newY = newPosition.y.coerceAtLeast(0f)
                            moveTo(Offset(newX, newY), robotID)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = "Up",
                            modifier = Modifier.size(arrowSize)
                        )
                    }
                }
                Row {
                    IconButton(
                        onClick = {
                            val newPosition = robotViewModel.getNextAvailableBox(Direction.Left)
                            val newX = newPosition.x.coerceAtLeast(0f)
                            val newY = targetPos.y
                            moveTo(Offset(newX, newY), robotID)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Left",
                            modifier = Modifier.size(arrowSize)
                        )
                    }

                    Spacer(modifier = Modifier.width(48.dp)) // gap between left/right

                    IconButton(
                        onClick = {
                            val newPosition = robotViewModel.getNextAvailableBox(Direction.Right)
                            val newX = newPosition.x.coerceAtMost(columns - 1f)
                            val newY = targetPos.y
                            moveTo(Offset(newX, newY), robotID)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Right",
                            modifier = Modifier.size(arrowSize)
                        )
                    }
                }
                Row {
                    IconButton(
                        onClick = {
                            val newPosition = robotViewModel.getNextAvailableBox(Direction.Down)
                            val newX = targetPos.x
                            val newY = newPosition.y.coerceAtMost(rows - 1f)
                            moveTo(Offset(newX, newY), robotID)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Down",
                            modifier = Modifier.size(arrowSize)
                        )
                    }
                }
            }
        }




    }
}