package com.example.ricochetrobots

import android.os.Bundle
import android.widget.Spinner
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.ricochetrobots.ui.theme.composable.gameboard.AnimatedGridMovement

class MainActivity : ComponentActivity() {

    private val robotViewModel: RobotViewModel = RobotViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    AppNavHost(robotViewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        robotViewModel.disconnectFromSocket()
        super.onDestroy()
    }
}

@Composable
fun AppNavHost(robotViewModel: RobotViewModel) {
    val navController = rememberNavController()

    // Collect navigation events from the ViewModel
    LaunchedEffect(Unit) {
        robotViewModel.navigationEvents.collect { route ->
            navController.navigate(route)
        }
    }

    NavHost(navController, startDestination = "join") {
        composable("join") {
            JoinComposable(robotViewModel = robotViewModel, {
                robotViewModel.joinGame()
//                navController.navigate("game")
            })
        }
        composable("game") {
            AnimatedGridMovement(
                robotViewModel,
//                onGameOver = { navController.popBackStack("join", inclusive = false) }
            )
        }
    }
}

@Composable
fun JoinComposable(robotViewModel: RobotViewModel, onJoined: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column (verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)){
            Text(
                text = "Ricochet Robots Multiplayer",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.padding(vertical = 20.dp))
            if (!robotViewModel.isJoiningGame.value) {
                Box(contentAlignment = Alignment.Center) {
                    Button(onClick = { onJoined() }) {
                        Text("Join Game")
                    }
                }
            }
            else {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }

        }
    }

}
