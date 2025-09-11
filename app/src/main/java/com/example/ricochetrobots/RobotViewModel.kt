package com.example.ricochetrobots

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import io.socket.client.IO as SocketIO
import io.socket.client.Socket
import java.util.UUID
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class RobotState(
    val id: Int,
    var isSelected: MutableState<Boolean> = mutableStateOf(false),
    val prevPos: MutableState<Offset> = mutableStateOf(Offset.Zero),
    val targetPos: MutableState<Offset> = mutableStateOf(Offset.Zero),
    val initialPos: MutableState<Offset> = mutableStateOf(Offset.Zero),
    val animProgress: Animatable<Float, AnimationVector1D> = Animatable(1f)
)

data class TileBlockState(
    val gridState: MutableState<Array<Array<Int>>> = mutableStateOf(emptyArray()),
)

enum class Direction(val dx: Int, val dy: Int) {
    Right(1, 0),
    Left(-1, 0),
    Up(0, -1),
    Down(0, 1);
}


class RobotViewModel : ViewModel() {
    private val _navigationEvents = MutableSharedFlow<String>()
    val navigationEvents = _navigationEvents.asSharedFlow()
    val columns = 10
    val rows = 10
    var robots by mutableStateOf(
        List(3) { index ->
            RobotState(id = index)
        }
    )
        private set

    var tileBlockState: TileBlockState = TileBlockState()

    var targetPos: MutableState<Offset> = mutableStateOf(Offset.Zero)

    var isJoiningGame: MutableState<Boolean> = mutableStateOf(false);
    var isGameOver: MutableState<Boolean> = mutableStateOf(false);

    private lateinit var socket: Socket

    init {
        initNewPositions()
        connectSocket()
    }

    fun initNewPositions() {
        generateRandomPositions()
        val grid = tileBlockState.gridState.value
        val gridString = buildString {
            for (row in grid) {
                append(row.joinToString(separator = " ") { it.toString() })
                append("\n")
            }
        }
        Log.d("Kevin tile grid", "\n$gridString")
    }

    fun joinGame() {
        val data = JSONObject()
        data.put("username", JSONObject.NULL)
        isJoiningGame.value = true
        viewModelScope.launch(Dispatchers.IO) {
            socket.emit("join_game", data)
        }
    }

    private fun parseBoard(obj: JSONObject) {
        // 1. Parse grid
        val gridJson = obj.getJSONArray("grid")
        val grid = (0 until gridJson.length()).map { r ->
            val row = gridJson.getJSONArray(r)
            (0 until row.length()).map { c -> row.getInt(c) }
        }

        // Convert List<List<Int>> → Array<Array<Int>>
        val gridArray = grid.map { it.toTypedArray() }.toTypedArray()

        // 2. Parse robots
        val robotsJson = obj.getJSONArray("robots")
        val robotsParsed = (0 until robotsJson.length()).map { i ->
            val arr = robotsJson.getJSONArray(i)
            arr.getInt(0) to arr.getInt(1) // (row, col)
        }

        // 3. Parse target
        val targetJson = obj.getJSONArray("target")
        val targetParsed = targetJson.getInt(1) to targetJson.getInt(0)

        tileBlockState = TileBlockState(mutableStateOf(gridArray))

        robots = robotsParsed.mapIndexed { index, (col, row) ->
            RobotState(
                id = index,
                prevPos = mutableStateOf(Offset(col.toFloat(), row.toFloat())),
                targetPos = mutableStateOf(Offset(col.toFloat(), row.toFloat())),
                initialPos = mutableStateOf(Offset(col.toFloat(), row.toFloat()))
            )
        }

        targetPos.value = Offset(targetParsed.second.toFloat(), targetParsed.first.toFloat())
    }

    private fun connectSocket() {
        try {
            socket = SocketIO.socket("http://10.0.2.2:5000")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        socket.on(Socket.EVENT_CONNECT) {
            println("Connected to server!")
        }

        socket.on("server_msg") { args ->
            if (args.isEmpty()) return@on

            val data = when {
                args[0] is String && args.size > 1 -> { // broadcast via room
                    JSONObject(args[1].toString())
                }
                else -> { // direct emit
                    JSONObject(args[0].toString())
                }
            }

            val msg = data.optString("message")
            println("kevin Server msg: $msg")
        }

        socket.on("game_waiting") { args ->
            if (args.isNotEmpty()) {
                val msg = args[0]
                println("kevin data ${msg}")
                try {
                    val json = msg as JSONObject
                    val msg1 = json.getString("game_waiting")
                    println("Server msg: $msg1")

                    // Must switch back to main thread for navigation
                    viewModelScope.launch(Dispatchers.Main) {
                        _navigationEvents.emit("game")
                    }
                } catch (e: Exception) {
                    println("Failed to parse server message: $msg")
                }
            }
        }

        socket.on("game_start") { args ->
            isJoiningGame.value = false;
            println("kevin GAME STARTING with ${args[0]}")
            val msg = args[0] as JSONObject
            val board = JSONObject(msg.getString("board"))
            println("kevin board received $board")

            parseBoard(board)

            // Must switch back to main thread for navigation
            viewModelScope.launch(Dispatchers.Main) {
                _navigationEvents.emit("game")
            }
        }

        socket.connect()
    }

    private fun getMinManhattanDistanceToTarget(): Int {
        var minDistance = 1000
        robots.forEach { robot ->
            minDistance = min(
                minDistance,
                (abs(robot.targetPos.value.x - targetPos.value.x) + abs(robot.targetPos.value.y - targetPos.value.y)).toInt()
            )
        }

        return minDistance
    }

    private fun generateRandomPositions() {
        robots.forEach { robot ->
            robot.prevPos.value = Offset(
                x = (0..<columns).random().toFloat(),
                y = (0..<rows).random().toFloat()
            )
            robot.targetPos.value = Offset(
                x = robot.prevPos.value.x,
                y = robot.prevPos.value.y,
            )
            robot.initialPos.value = Offset(
                x = robot.prevPos.value.x,
                y = robot.prevPos.value.y,
            )
        }

        // ensure manhattan distance between any robot and the target is at least 3
        do {
            targetPos.value = Offset(
                x = (0 until columns).random().toFloat(),
                y = (0 until rows).random().toFloat()
            )
            Log.d("kevin target pos generation", "${targetPos.value}")
        } while (getMinManhattanDistanceToTarget() < 3)
    }

    fun resetBoard() {
        robots.forEach { robot ->
            robot.targetPos.value = robot.initialPos.value
            robot.prevPos.value = robot.initialPos.value
        }
    }

    fun exitGame() {
        viewModelScope.launch {
            _navigationEvents.emit("join")
        }
    }


    // Helper to get currently selected robot
    fun getSelectedRobot(): RobotState? {
        return robots.firstOrNull { it.isSelected.value }
    }

    // Check if a position is free (no other robot there)
    private fun isPositionFree(x: Int, y: Int, ignoreRobotId: Int, direction: Direction): Boolean {
        val noRobotsInSpace = robots.none {
            it.id != ignoreRobotId &&
                    it.targetPos.value.x.roundToInt() == x &&
                    it.targetPos.value.y.roundToInt() == y
        }

        val isWallBlocking = when (direction){
            Direction.Up -> {
                // check if bottom block's top side is blocked
                (tileBlockState.gridState.value[y+1][x] and 1) != 0
            }
            Direction.Right -> {
                // check if left is blocked
                (tileBlockState.gridState.value[y][x] and 8) != 0
            }
            Direction.Down -> {
                // check if top is blocked
                (tileBlockState.gridState.value[y][x] and 1) != 0
            }
            Direction.Left -> {
                // check if right block's left side is blocked
                (tileBlockState.gridState.value[y][x+1] and 8) != 0
            }
        }

        return (noRobotsInSpace and !isWallBlocking)
    }

    fun checkIsWinningState() {
        robots.forEach { robot ->
            if (robot.targetPos.value == targetPos.value) {
                Log.d("kevin is winning state", "true for robot ${robot.id} and target ${targetPos.value} and current ${robot.targetPos.value}")
                isGameOver.value = true
                return
            }
        }
        isGameOver.value = false
    }

    fun getNextAvailableBox(direction: Direction, robot: RobotState? = getSelectedRobot()): Offset {
        if (robot == null) {
            throw Exception("robot not defined in getNextAvailableBox");
        }

        var x = robot.targetPos.value.x.toInt()
        var y = robot.targetPos.value.y.toInt()

        val deltaX = direction.dx
        val deltaY = direction.dy

        // Move right until blocked or end of grid
        while (x + deltaX < columns && y + deltaY < rows && isPositionFree(x + deltaX, y + deltaY, robot.id, direction)) {
            x += deltaX
            y += deltaY
        }

        return Offset(x.toFloat(), y.toFloat())
    }

    data class Node(
        val positions: List<Offset>, // robot positions in this node
        val moves: List<Pair<Int, Direction>> = emptyList() // moves to reach this state
    )

    private fun encodeNode(positions: List<Offset>): Long {
        val config = BitPackedVisitedConfig(rows, positions.size)
        positions.forEachIndexed { robotId, pos ->
            config.setBit(pos.y.toInt(), pos.x.toInt(), robotId)
        }
        return config.packToLong()
    }

    fun solve() {
        val startPositions = robots.map { it.targetPos.value }
        val queue = ArrayDeque<Node>()
        val visited = HashSet<Long>()

        queue.add(Node(startPositions))
        visited.add(encodeNode(startPositions))
        Log.d("kevin visited init state", "${encodeNode(startPositions)}")

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()

            // Check if any robot reached the target
            if (current.positions.any { it == targetPos.value }) {
                Log.d("kevin Solve", "Solution found: ${current.moves}")
                return
            }

            // Generate all next states
            for ((robotId, _) in current.positions.withIndex()) {
                for (dir in Direction.entries) {
                    val nextPos = getNextAvailableBoxBFS(dir, robotId, current.positions)
                    if (nextPos != current.positions[robotId]) {
                        val newPositions = current.positions.toMutableList()
                        newPositions[robotId] = nextPos

                        val tempState = encodeNode(newPositions)
                        if (tempState in visited) {
                            continue
                        }
                        visited.add(tempState)

                        queue.add(Node(newPositions, current.moves + (robotId to dir)))
                    }
                }
            }
        }

        Log.d("kevin Solve", "No solution found")
    }

    // BFS helper functions
    private fun isPositionFreeBFS(
        x: Int, y: Int, ignoreRobotId: Int, direction: Direction, positions: List<Offset>
    ): Boolean {
        val noRobotsInSpace = positions.withIndex().none { (id, pos) ->
            id != ignoreRobotId && pos.x.roundToInt() == x && pos.y.roundToInt() == y
        }

        val isWallBlocking = when (direction) {
            Direction.Up -> (tileBlockState.gridState.value[y + 1][x] and 1) != 0
            Direction.Right -> (tileBlockState.gridState.value[y][x] and 8) != 0
            Direction.Down -> (tileBlockState.gridState.value[y][x] and 1) != 0
            Direction.Left -> (tileBlockState.gridState.value[y][x + 1] and 8) != 0
        }

        return noRobotsInSpace && !isWallBlocking
    }

    private fun getNextAvailableBoxBFS(
        direction: Direction, robotId: Int, positions: List<Offset>
    ): Offset {
        var x = positions[robotId].x.toInt()
        var y = positions[robotId].y.toInt()
        val deltaX = direction.dx
        val deltaY = direction.dy

        while (x + deltaX in 0 until columns &&
            y + deltaY in 0 until rows &&
            isPositionFreeBFS(x + deltaX, y + deltaY, robotId, direction, positions)) {
            x += deltaX
            y += deltaY
        }

        return Offset(x.toFloat(), y.toFloat())
    }
}

