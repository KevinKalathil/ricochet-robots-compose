package com.example.ricochetrobots

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.ricochetrobots.ui.theme.composable.gameboard.AnimatedGridMovement

class MainActivity : ComponentActivity() {

    private val robotViewModel: RobotViewModel = RobotViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    AnimatedGridMovement(robotViewModel)
                }
            }
        }
    }
}



