import kotlin.math.abs

fun main() {
    data class Coordinate(val x: Int, val y: Int)

    fun Coordinate.distanceTo(other: Coordinate): Int = abs(x - other.x) + abs(y - other.y)

    data class Reading(val sensorPosition: Coordinate, val beaconPosition: Coordinate) {
        val distance = sensorPosition.distanceTo(beaconPosition)
    }

    val readingPattern = Regex("""Sensor at x=(-?\d+), y=(-?\d+): closest beacon is at x=(-?\d+), y=(-?\d+)""")

    fun readPositions(input: List<String>): List<Reading> = input.map { line ->
        val (sensorX, sensorY, beaconX, beaconY) = readingPattern.matchEntire(line)!!
            .groupValues
            .drop(1)
            .map { it.toInt() }
        Reading(Coordinate(sensorX, sensorY), Coordinate(beaconX, beaconY))
    }

    fun part1(input: List<String>, targetY: Int): Int {
        val readings = readPositions(input)
        val beaconsInTargetRow = readings.asSequence()
            .map { it.beaconPosition }
            .filter { it.y == targetY }
            .map { it.x }
            .toSet()
        return readings
            .flatMap { (beacon, sensor) ->
                val distance = beacon.distanceTo(sensor)
                val yDistanceToTarget = abs(beacon.y - targetY)
                val possibleXDistance = distance - yDistanceToTarget
                beacon.x - possibleXDistance..beacon.x + possibleXDistance
            }
            .subtract(beaconsInTargetRow)
            .size
    }

    fun part2(input: List<String>, maxCoordinate: Int): Long {
        val readings = readPositions(input).sortedByDescending { it.distance }
        for (x in 0..maxCoordinate) {
            var y = 0
            while (y <= maxCoordinate) {
                val c = Coordinate(x, y)
                val reading = readings.firstOrNull { c.distanceTo(it.sensorPosition) <= it.distance }
                if (reading == null) {
                    return x.toLong() * 4_000_000 + y
                }
                val maxYDistance = reading.distance - abs(c.x - reading.sensorPosition.x)
                y = reading.sensorPosition.y + maxYDistance + 1
            }
        }
        error("not found")
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day15_test")
    check(part1(testInput, 10) == 26)
    check(part2(testInput, 20) == 56000011L)

    val input = readInput("Day15")
    println(part1(input, 2_000_000))
    println(part2(input, 4_000_000))
}
