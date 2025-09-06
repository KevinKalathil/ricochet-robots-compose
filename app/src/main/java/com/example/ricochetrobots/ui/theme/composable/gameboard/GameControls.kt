package com.example.ricochetrobots.ui.theme.composable.gameboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.ricochetrobots.Direction
import com.example.ricochetrobots.RobotViewModel

@Composable
fun GameControls(
    robotViewModel: RobotViewModel,
    targetPos: Offset,
    rows: Int,
    columns: Int,
    moveTo: (Offset) -> Unit
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                val newPosition = (robotViewModel.getNextAvailableBox(Direction.Left))
                val newX = newPosition.x.coerceAtLeast(0f)
                val newY = targetPos.y
                moveTo(Offset(newX ?: 0f, newY))
            }) { Text("Left") }

            Button(onClick = {
                val newPosition = (robotViewModel.getNextAvailableBox(Direction.Right))
                val newX = newPosition.x.coerceAtMost(columns - 1f)
                val newY = targetPos.y
                moveTo(Offset(newX ?: 0f, newY))
            }) { Text("Right") }

            Button(onClick = {
                val newPosition = (robotViewModel.getNextAvailableBox(Direction.Up))
                val newX = targetPos.x
                val newY = newPosition.y.coerceAtLeast(0f)
                moveTo(Offset(newX, newY ?: 0f))
            }) { Text("Up") }

            Button(onClick = {
                val newPosition = (robotViewModel.getNextAvailableBox(Direction.Down))
                val newX = targetPos.x
                val newY = newPosition.y.coerceAtMost(rows - 1f)

                moveTo(Offset(newX, newY ?: 0f))
            }) { Text("Down") }

        }
        Row {
            Button(onClick = {
                robotViewModel.generateRandomTileBlocks()
            }) { Text("Regenerate") }

            Button(onClick = {
                robotViewModel.resetBoard()
            }) { Text("Reset") }

            Button(onClick = {
                robotViewModel.solve()
            }) { Text("Solve") }
        }
        Row {
            Button(onClick = {
                robotViewModel.initNewPositions()
            }) { Text("Init") }
            Text(robotViewModel.getSelectedRobot()?.id.toString())
        }

    }
}