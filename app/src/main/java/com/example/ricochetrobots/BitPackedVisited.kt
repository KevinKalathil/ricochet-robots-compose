package com.example.ricochetrobots

class BitPackedVisitedConfig(private val rows: Int, private val cols: Int, private val numRobots: Int) {
    private val byteArray = ByteArray(numRobots)

    fun setBit(row: Int, col: Int, robotId: Int) {
        val bit = row * rows + col
        byteArray[robotId] = bit.toByte()
    }

    fun packToLong(): Long {
        var result = 0L
        for (i in byteArray) {
            result = (result shl 8) or (i.toLong() and 0xFF)
        }
        return result
    }

}
