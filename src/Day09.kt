import kotlin.math.abs
import kotlin.math.sign

private enum class Direction {
    LEFT, RIGHT, UP, DOWN
}

fun main() {
    data class Point(val x: Int, val y: Int)
    data class Knot(var position: Point)

    fun Point.move(direction: Direction) = when (direction) {
        Direction.LEFT -> copy(x = x - 1)
        Direction.RIGHT -> copy(x = x + 1)
        Direction.UP -> copy(y = y + 1)
        Direction.DOWN -> copy(y = y - 1)
    }

    fun String.toDirection(): Direction = when (this) {
        "L" -> Direction.LEFT
        "R" -> Direction.RIGHT
        "U" -> Direction.UP
        "D" -> Direction.DOWN
        else -> throw IllegalArgumentException()
    }

    class Rope(numberOfKnots: Int) {
        private var knots = Point(0, 0).let { p -> List(numberOfKnots) { Knot(p) } }
        private val tailPositions = mutableSetOf(knots.last().position)
        val visitedTailPositions
            get() = tailPositions.size

        fun moveHead(direction: Direction) {
            knots.first().apply { position = position.move(direction) }
            for ((head, tail) in knots.asSequence().windowed(2)) {
                val xDelta = head.position.x - tail.position.x
                val yDelta = head.position.y - tail.position.y
                val xUpdate = if (abs(xDelta).let { it > 1 || it == 1 && abs(yDelta) > 1 }) xDelta.sign else 0
                val yUpdate = if (abs(yDelta).let { it > 1 || it == 1 && abs(xDelta) > 1 }) yDelta.sign else 0
                if (xUpdate == 0 && yUpdate == 0) return
                tail.apply { position = Point(position.x + xUpdate, position.y + yUpdate) }
            }
            tailPositions.add(knots.last().position)
        }
    }

    fun readDirections(input: List<String>): List<Direction> = input.flatMap {
        val (d, s) = it.split(' ')
        val direction = d.toDirection()
        val steps = s.toInt()
        List(steps) { direction }
    }

    fun part1(input: List<String>): Int {
        val rope = Rope(2)
        readDirections(input).forEach {
            rope.moveHead(it)
        }
        return rope.visitedTailPositions
    }

    fun part2(input: List<String>): Int {
        val rope = Rope(10)
        readDirections(input).forEach {
            rope.moveHead(it)
        }
        return rope.visitedTailPositions
    }

    // test if implementation meets criteria from the description, like:
    check(part1(readInput("Day09_test")) == 13)
    check(part2(readInput("Day09_test2")) == 36)

    val input = readInput("Day09")
    println(part1(input))
    println(part2(input))
}
